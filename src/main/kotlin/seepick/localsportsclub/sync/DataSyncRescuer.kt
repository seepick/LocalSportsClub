package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.PhpSessionId
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.activity.ActivityDetails
import seepick.localsportsclub.api.activity.FreetrainingDetails
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.FreetrainingState
import java.time.Month

interface DataSyncRescuer {
    suspend fun fetchInsertAndDispatchActivity(
        session: PhpSessionId,
        city: City,
        activityId: Int,
        venueSlug: String,
        prefilledVenueNotes: String
    ): ActivityDbo

    suspend fun fetchInsertAndDispatchFreetraining(
        session: PhpSessionId,
        city: City,
        freetrainingId: Int,
        venueSlug: String,
        prefilledNotes: String
    ): FreetrainingDbo
}

class DataSyncRescuerImpl(
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val uscApi: UscApi,
    private val venueRepo: VenueRepo,
    private val venueSyncInserter: VenueSyncInserter,
    private val dispatcher: SyncerListenerDispatcher,
    private val clock: Clock,
) : DataSyncRescuer {
    private val log = logger {}

    override suspend fun fetchInsertAndDispatchActivity(
        session: PhpSessionId,
        city: City,
        activityId: Int,
        venueSlug: String,
        prefilledVenueNotes: String,
    ): ActivityDbo {
        log.debug { "Trying to rescue locally non-existing activity with ID $activityId for venue [$venueSlug]" }
        require(activityRepo.selectById(activityId) == null)
        val activityDetails = uscApi.fetchActivityDetails(session, activityId).let(::adjustDate)

        val venue = ensureVenue(session, city, venueSlug, prefilledVenueNotes)
        val dbo = activityDetails.toActivityDbo(activityId, venue.id)
        activityRepo.insert(dbo)
        dispatcher.dispatchOnActivityDbosAdded(listOf(dbo))
        return dbo
    }

    private fun adjustDate(details: ActivityDetails): ActivityDetails =
        if (clock.today().month == Month.JANUARY && details.dateTimeRange.from.month == Month.DECEMBER) {
            log.debug { "Adjusting date by one year behind for 'flip-over activity'." }
            details.copy(dateTimeRange = details.dateTimeRange.minusOneYear())
        } else details

    private fun ActivityDetails.toActivityDbo(activityId: Int, venueId: Int) = ActivityDbo(
        id = activityId,
        venueId = venueId,
        name = name,
        category = category,
        spotsLeft = spotsLeft,
        from = dateTimeRange.from,
        to = dateTimeRange.to,
        teacher = null,
        state = ActivityState.Blank,
        cancellationLimit = cancellationDateLimit,
        planId = plan.id,
    )

    private suspend fun ensureVenue(
        session: PhpSessionId,
        city: City,
        venueSlug: String,
        prefilledNotes: String
    ): VenueDbo =
        venueRepo.selectBySlug(venueSlug) ?: suspend {
            venueSyncInserter.fetchInsertAndDispatch(
                session, city, listOf(VenueMeta(slug = venueSlug, plan = null)), prefilledNotes
            )
            venueRepo.selectBySlug(venueSlug)
                ?: error("Terribly failed rescuing venue: [$venueSlug]")
        }()

    override suspend fun fetchInsertAndDispatchFreetraining(
        session: PhpSessionId,
        city: City,
        freetrainingId: Int,
        venueSlug: String,
        prefilledNotes: String
    ): FreetrainingDbo {
        log.debug { "Trying to rescue locally non-existing freetraining $freetrainingId for venue [$venueSlug]" }
        require(freetrainingRepo.selectById(freetrainingId) == null)
        val freetrainingDetail = uscApi.fetchFreetrainingDetails(session, freetrainingId).let(::adjustDate)
        val venue = ensureVenue(session, city, venueSlug, prefilledNotes)
        val dbo = freetrainingDetail.toFreetrainingDbo(freetrainingId = freetrainingId, venueId = venue.id)
        freetrainingRepo.insert(dbo)
        dispatcher.dispatchOnFreetrainingDbosAdded(listOf(dbo))
        return dbo
    }

    private fun adjustDate(details: FreetrainingDetails): FreetrainingDetails =
        if (clock.today().month == Month.JANUARY && details.date.month == Month.DECEMBER) {
            log.debug { "Adjusting date by one year behind for 'flip-over freetraining'." }
            details.copy(date = details.date.minusYears(1))
        } else details

    private fun FreetrainingDetails.toFreetrainingDbo(freetrainingId: Int, venueId: Int) = FreetrainingDbo(
        id = freetrainingId,
        venueId = venueId,
        name = name,
        category = category,
        date = date,
        state = FreetrainingState.Blank,
        planId = plan.id,
    )
}
