package seepick.localsportsclub.sync.domain

import com.github.seepick.uscclient.UscApi
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.sync.SyncProgress
import seepick.localsportsclub.sync.Syncer
import seepick.localsportsclub.sync.SyncerListener
import seepick.localsportsclub.sync.SyncerListenerDispatcher
import java.time.LocalDate
import java.time.LocalDateTime

class SyncerFacade(
    private val venueSyncer: VenueSyncer,
    private val activitiesSyncer: ActivitiesSyncer,
    private val freetrainingSyncer: FreetrainingSyncer,
    private val scheduleSyncer: ScheduleSyncer,
    private val checkinSyncer: CheckinSyncer,
    private val cleanupPostSync: CleanupPostSync,
    private val dispatcher: SyncerListenerDispatcher,
    private val singlesService: SinglesService,
    private val clock: Clock,
    private val progress: SyncProgress,
    private val venueAutoSyncer: VenueAutoSyncer,
    private val api: UscApi,
    private val syncDaysAhead: Int,
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
        var successfullyFinished = false
        try {
            suspendTransaction {
                safeSync()
                successfullyFinished = true
            }
        } finally {
            progress.stop(isError = !successfullyFinished)
        }
    }

    private val plan by lazy {
        runBlocking {
            api.fetchMembership().plan
        }
    }

    private suspend fun safeSync() {
        progress.onProgress("short", "long123456789longSuper1234long123456789long123456789long123456789")
        delay(10_000)
        return

        val now = clock.now()
        val city = singlesService.preferences.city ?: error("No city defined!")
        val lastSync = singlesService.getLastSyncFor(city)
        val days = calculateDaysToSync(clock.today(), syncDaysAhead, lastSync)
        val isFullSync = lastSync == null || lastSync.toLocalDate() != now.toLocalDate()
        log.debug { "isFullSync=$isFullSync, lastSync=$lastSync, days=$days" }

        var insertedActivities = emptyList<ActivityDbo>()
        if (isFullSync) {
            venueSyncer.sync(plan, city)
            insertedActivities = activitiesSyncer.sync(plan, city, days)
            freetrainingSyncer.sync(plan, city, days)
        }
        scheduleSyncer.sync(city)
        checkinSyncer.sync(city)
        if (isFullSync) {
            cleanupPostSync.cleanup()
            venueAutoSyncer.syncAllDetails(insertedActivities) // after cleanup
        }
        singlesService.setLastSyncFor(city, now)
    }

}

