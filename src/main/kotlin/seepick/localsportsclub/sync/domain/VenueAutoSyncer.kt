package seepick.localsportsclub.sync.domain

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.ActivityDetailService
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.sync.SyncProgress

class VenueAutoSyncer(
    private val singlesService: SinglesService,
    private val venueRepo: VenueRepo,
    private val activityDetailService: ActivityDetailService,
    private val progress: SyncProgress,
) {
    private val log = logger {}

    suspend fun syncAllDetails(insertedActivities: List<ActivityDbo>) {
        progress.onProgress("Auto-Sync")
        val city = singlesService.preferences.city ?: error("No city defined!")
        val autoSyncedVenueIds = venueRepo.selectAllByCity(city.id).filter { it.isAutoSync }.map { it.id }.toSet()
        val toBeSyncedActivities = insertedActivities.filter { autoSyncedVenueIds.contains(it.venueId) }
        log.debug { "Auto sync details for ${autoSyncedVenueIds.size} venues / ${toBeSyncedActivities.size} activities." }
        activityDetailService.syncBulk(toBeSyncedActivities.map { it.id })
    }
}
