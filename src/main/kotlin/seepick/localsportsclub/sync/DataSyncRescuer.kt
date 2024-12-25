package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.activity.ActivityApi
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.VenueRepo

interface DataSyncRescuer {
    suspend fun rescueActivity(activityId: Int, venueSlug: String, prefilledNotes: String): ActivityDbo
}

class DataSyncRescuerImpl(
    private val activityRepo: ActivityRepo,
    private val activityApi: ActivityApi,
    private val venueRepo: VenueRepo,
    private val venueSyncInserter: VenueSyncInserter,
    private val dispatcher: SyncerListenerDispatcher,
) : DataSyncRescuer {
    private val log = logger {}

    override suspend fun rescueActivity(activityId: Int, venueSlug: String, prefilledNotes: String): ActivityDbo {
        log.debug { "Trying to rescue locally non-existing activity $activityId for venue [$venueSlug]" }
        require(activityRepo.selectById(activityId) == null)

        val activityDetails = activityApi.fetchDetails(activityId)
        val venue = venueRepo.selectBySlug(venueSlug) ?: suspend {
            venueSyncInserter.fetchAllInsertDispatch(listOf(venueSlug), prefilledNotes)
            venueRepo.selectBySlug(venueSlug)
                ?: error("Terribly failed rescuing venue: $venueSlug for activity $activityId")
        }()

        val dbo = ActivityDbo(
            id = activityId,
            venueId = venue.id,
            name = activityDetails.name,
            category = activityDetails.category,
            spotsLeft = activityDetails.spotsLeft,
            from = activityDetails.dateTimeRange.start,
            to = activityDetails.dateTimeRange.end,
            isBooked = false,
            wasCheckedin = false,
        )
        activityRepo.insert(dbo)
        dispatcher.dispatchOnActivityDboAdded(dbo)
        return dbo
    }
}
