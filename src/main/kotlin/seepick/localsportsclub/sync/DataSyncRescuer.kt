package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.activity.ActivityApi
import seepick.localsportsclub.api.activity.ActivityDetails
import seepick.localsportsclub.api.activity.FreetrainingDetails
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo

interface DataSyncRescuer {
    suspend fun rescueActivity(activityId: Int, venueSlug: String, prefilledNotes: String): ActivityDbo
    suspend fun rescueFreetraining(freetrainingId: Int, venueSlug: String, prefilledNotes: String): FreetrainingDbo
}

class DataSyncRescuerImpl(
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
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
        val venue = ensureVenue(venueSlug, prefilledNotes)
        val dbo = activityDetails.toActivityDbo(activityId, venue.id)
        activityRepo.insert(dbo)
        dispatcher.dispatchOnActivityDbosAdded(listOf(dbo))
        return dbo
    }

    private fun ActivityDetails.toActivityDbo(activityId: Int, venueId: Int) = ActivityDbo(
        id = activityId,
        venueId = venueId,
        name = name,
        category = category,
        spotsLeft = spotsLeft,
        from = dateTimeRange.from,
        to = dateTimeRange.to,
        teacher = null,
        isBooked = false,
        wasCheckedin = false,
    )

    private suspend fun ensureVenue(venueSlug: String, prefilledNotes: String): VenueDbo =
        venueRepo.selectBySlug(venueSlug) ?: suspend {
            venueSyncInserter.fetchAllInsertDispatch(listOf(venueSlug), prefilledNotes)
            venueRepo.selectBySlug(venueSlug)
                ?: error("Terribly failed rescuing venue: [$venueSlug]")
        }()

    override suspend fun rescueFreetraining(
        freetrainingId: Int,
        venueSlug: String,
        prefilledNotes: String
    ): FreetrainingDbo {
        log.debug { "Trying to rescue locally non-existing freetraining $freetrainingId for venue [$venueSlug]" }
        require(freetrainingRepo.selectById(freetrainingId) == null)
        val freetrainingDetail = activityApi.fetchFreetrainingDetails(freetrainingId)
        val venue = ensureVenue(venueSlug, prefilledNotes)
        val dbo = freetrainingDetail.toFreetrainingDbo(freetrainingId, venue.id)
        freetrainingRepo.insert(dbo)
        dispatcher.dispatchOnFreetrainingDbosAdded(listOf(dbo))
        return dbo
    }

    private fun FreetrainingDetails.toFreetrainingDbo(freetrainingId: Int, venueId: Int) = FreetrainingDbo(
        id = freetrainingId,
        venueId = venueId,
        name = name,
        category = category,
        date = date,
        wasCheckedin = false,
    )
}
