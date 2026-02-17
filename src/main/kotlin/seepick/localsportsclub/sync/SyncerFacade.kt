package seepick.localsportsclub.sync

import com.github.seepick.uscclient.UscApi
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.ActivityDetailService
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.sync.domain.ActivitiesSyncer
import seepick.localsportsclub.sync.domain.CheckinSyncer
import seepick.localsportsclub.sync.domain.FreetrainingSyncer
import seepick.localsportsclub.sync.domain.ScheduleSyncer
import seepick.localsportsclub.sync.domain.VenueSyncer
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
        val now = clock.now()
        val city = singlesService.preferences.city ?: error("No city defined!")
        val lastSync = singlesService.getLastSyncFor(city)
        val days = calculateDaysToSync(clock.today(), syncDaysAhead, lastSync)
        val isFullSync = lastSync == null || lastSync.toLocalDate() != now.toLocalDate()
        log.debug { "isFullSync=$isFullSync, lastSync=$lastSync, days=$days" }

        if (isFullSync) {
            venueSyncer.sync(plan, city)
            activitiesSyncer.sync(plan, city, days)
            freetrainingSyncer.sync(plan, city, days)
        }
        scheduleSyncer.sync(city)
        checkinSyncer.sync(city)
        if (isFullSync) {
            cleanupPostSync.cleanup()
            venueAutoSyncer.syncAllDetails() // after cleanup
        }
        singlesService.setLastSyncFor(city, now)
    }

}

class VenueAutoSyncer(
    private val singlesService: SinglesService,
    private val venueRepo: VenueRepo,
    private val activityRepo: ActivityRepo,
    private val activityDetailService: ActivityDetailService,
    private val progress: SyncProgress,
    private val clock: Clock,
) {
    private val log = logger {}

    suspend fun syncAllDetails() {
        progress.onProgress("Auto-Sync")
        val city = singlesService.preferences.city ?: error("No city defined!")
        val autoSyncedVenues = venueRepo.selectAllByCity(city.id).filter { it.isAutoSync }

        val toBeSyncedActivities = autoSyncedVenues.flatMap { venue ->
            log.debug { "auto-sync for all activities w/o teacher for: [${venue.name}]" }
            activityRepo.selectAllForVenueId(venue.id).filter {
                it.from.toLocalDate() >= clock.today() && // only future activities
                        // FIXME better way to check just teacher null
                        it.teacher == null
                // because if sync doesn't return teacher, will always be resynced.
                // also: maybe want to resync for spots left...?
            }
        }

        activityDetailService.syncBulk(toBeSyncedActivities.map { it.id })
    }
}
