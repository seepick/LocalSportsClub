package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.activities.ActivityApi
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.VenueRepo

class DataSyncRescuer(
    private val activityRepo: ActivityRepo,
    private val activityApi: ActivityApi,
    private val venueRepo: VenueRepo,
    private val venueSyncInserter: VenueSyncInserter,
    private val dispatcher: SyncerListenerDispatcher,
) {
    private val log = logger {}

    suspend fun rescueActivity(activityId: Int, venueSlug: String): ActivityDbo {
        log.debug { "Trying to rescue locally non-existing activity $activityId for venue [$venueSlug]" }
        require(activityRepo.selectById(activityId) == null)

        val details = activityApi.fetchDetails(activityId)
        val venue = venueRepo.selectBySlug(venueSlug) ?: suspend {
            venueSyncInserter.fetchAllInsertDispatch(listOf(venueSlug))
            venueRepo.selectBySlug(venueSlug)
                ?: error("Terribly failed rescuing venue: $venueSlug for activity $activityId")
        }()

        val dbo = ActivityDbo(
            id = activityId,
            venueId = venue.id,
            name = details.name,
            category = details.category,
            spotsLeft = details.spotsLeft,
            from = details.dateTimeRange.start,
            to = details.dateTimeRange.end,
            scheduled = false,
        )
        activityRepo.insert(dbo)
        dispatcher.dispatchOnActivityDboAdded(dbo)
        return dbo
    }
}
