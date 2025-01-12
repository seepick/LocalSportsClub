package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.api.UscConfig
import seepick.localsportsclub.service.SinglesService
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.sync.thirdparty.ThirdPartySyncer
import java.time.LocalDate
import java.time.LocalDateTime

class SyncerFacade(
    private val venueSyncer: VenueSyncer,
    private val activitiesSyncer: ActivitiesSyncer,
    private val freetrainingSyncer: FreetrainingSyncer,
    private val scheduleSyncer: ScheduleSyncer,
    private val checkinSyncer: CheckinSyncer,
    private val thirdPartySyncer: ThirdPartySyncer,
    private val cleanupSyncer: CleanupSyncer,
    private val dispatcher: SyncerListenerDispatcher,
    private val singlesService: SinglesService,
    private val clock: Clock,
    private val uscConfig: UscConfig,
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
        val now = clock.now()
        val lastSync = singlesService.readLastSync()
        val days = calculateDaysToSync(clock.today(), uscConfig.syncDaysAhead, lastSync)
        transaction {
            runBlocking {
//                if (lastSync == null || lastSync.toLocalDate() != now.toLocalDate()) {
//                    venueSyncer.sync()
//                } else {
//                    log.debug { "Skip syncing venues as already did today (not assuming much change there)." }
//                }
//                activitiesSyncer.sync(days)
//                freetrainingSyncer.sync(days)
//                scheduleSyncer.sync()
//                checkinSyncer.sync()
                thirdPartySyncer.sync()
                cleanupSyncer.sync()
                singlesService.updateLastSync(now)
            }
        }
    }

}
