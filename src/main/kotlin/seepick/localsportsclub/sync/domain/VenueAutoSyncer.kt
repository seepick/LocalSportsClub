package seepick.localsportsclub.sync.domain

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.ActivityDetailService
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.sync.SyncProgress

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
                it.from.toLocalDate() >= clock.today() &&
                        // FIXME process inserted activities by sync instead
                        it.teacher == null
                // because if sync doesn't return teacher, will always be resynced.
                // also: maybe want to resync for spots left...?
            }
        }

        activityDetailService.syncBulk(toBeSyncedActivities.map { it.id })
    }
}
