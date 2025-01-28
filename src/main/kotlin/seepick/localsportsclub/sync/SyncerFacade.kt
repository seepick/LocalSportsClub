package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.api.PhpSessionProvider
import seepick.localsportsclub.api.PlanProvider
import seepick.localsportsclub.api.UscConfig
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.sync.thirdparty.ThirdPartySyncerAmsterdam
import java.time.LocalDate
import java.time.LocalDateTime

class SyncerFacade(
    private val venueSyncer: VenueSyncer,
    private val activitiesSyncer: ActivitiesSyncer,
    private val freetrainingSyncer: FreetrainingSyncer,
    private val scheduleSyncer: ScheduleSyncer,
    private val checkinSyncer: CheckinSyncer,
    private val thirdPartySyncerAmsterdam: ThirdPartySyncerAmsterdam,
    private val cleanupPostSync: CleanupPostSync,
    private val dispatcher: SyncerListenerDispatcher,
    private val singlesService: SinglesService,
    private val clock: Clock,
    private val uscConfig: UscConfig,
    private val phpSessionProvider: PhpSessionProvider,
    private val planProvider: PlanProvider,
    private val progress: SyncProgress,
) : Syncer {

    private val log = logger {}

    companion object {
        // visible for testing
        fun calculateDaysToSync(today: LocalDate, syncDaysAhead: Int, lastSync: LocalDateTime?): List<LocalDate> {
            val configuredSyncUntil = today.plusDays(syncDaysAhead.toLong())
            if (lastSync == null) {
                return today.datesUntil(configuredSyncUntil).toList()
            }
            val lastSyncDate = lastSync.toLocalDate()
            if (lastSyncDate == today) {
                return emptyList()
            }
            val lastSyncReachedDay = lastSyncDate.plusDays(syncDaysAhead.toLong())
            if (lastSyncReachedDay < today) {
                return today.datesUntil(configuredSyncUntil).toList()
            }
            val maxSyncReach = today.plusDays(syncDaysAhead.toLong())
            return lastSyncReachedDay.datesUntil(maxSyncReach).toList()
        }
    }

    override fun registerListener(listener: SyncerListener) {
        dispatcher.registerListener(listener)
    }

    override suspend fun sync() {
        log.debug { "Syncing ..." }
        progress.start()
        try {
//            newSuspendedTransaction {
            transaction {
                runBlocking {
                    safeSync()
                }
            }
        } finally {
            progress.stop()
        }
    }

    private suspend fun safeSync() {
        val now = clock.now()
        val city = singlesService.preferences.city ?: error("No city defined!")
        val lastSync = singlesService.getLastSyncFor(city)
        val days = calculateDaysToSync(clock.today(), uscConfig.syncDaysAhead, lastSync)
        val session = phpSessionProvider.provide()
        val plan = planProvider.provide(session)
        val isFullSync = lastSync == null || lastSync.toLocalDate() != now.toLocalDate()
        log.debug { "isFullSync=$isFullSync, lastSync=$lastSync, days=$days" }

        if (isFullSync) {
            venueSyncer.sync(session, plan, city)
            activitiesSyncer.sync(session, plan, city, days)
            freetrainingSyncer.sync(session, plan, city, days)
        }
        scheduleSyncer.sync(session, city)
        checkinSyncer.sync(session, city)
        if (isFullSync) {
            if (city == City.Amsterdam) {
                thirdPartySyncerAmsterdam.sync(days)
            }
            cleanupPostSync.cleanup()
        }

        singlesService.setLastSyncFor(city, now)
    }
}
