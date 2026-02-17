package seepick.localsportsclub.sync

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.activity.ActivityDetails
import com.github.seepick.uscclient.activity.FreetrainingDetails
import com.github.seepick.uscclient.model.City
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.sync.domain.VenueMeta
import seepick.localsportsclub.sync.domain.VenueSyncInserter
import java.time.Month

interface DataSyncRescuer {
    suspend fun fetchInsertAndDispatchActivity(
        city: City,
        activityId: Int,
        venueSlug: String,
        prefilledVenueNotes: String,
    ): ActivityDbo

    suspend fun fetchInsertAndDispatchFreetraining(
        city: City,
        freetrainingId: Int,
        venueSlug: String,
        prefilledNotes: String,
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
        city: City,
        activityId: Int,
        venueSlug: String,
        prefilledVenueNotes: String,
    ): ActivityDbo {
        log.debug { "Trying to rescue locally non-existing activity with ID $activityId for venue [$venueSlug]" }
        require(activityRepo.selectById(activityId) == null)
        val activityDetails = uscApi.fetchActivityDetails(activityId).let(::adjustDate)

        val venue = ensureVenue(city, venueSlug, prefilledVenueNotes)
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
        state = ActivityState.Blank,
        cancellationLimit = cancellationDateLimit,
        planId = plan.id,
        teacher = null,
        description = null,
    )

    private suspend fun ensureVenue(
        city: City,
        venueSlug: String,
        prefilledNotes: String,
    ): VenueDbo =
        venueRepo.selectBySlug(venueSlug) ?: suspend {
            venueSyncInserter.fetchInsertAndDispatch(
                city, listOf(VenueMeta(slug = venueSlug, plan = null)), prefilledNotes
            )
            venueRepo.selectBySlug(venueSlug)
                ?: error("Terribly failed rescuing venue: [$venueSlug]")
        }()

    override suspend fun fetchInsertAndDispatchFreetraining(
        city: City,
        freetrainingId: Int,
        venueSlug: String,
        prefilledNotes: String,
    ): FreetrainingDbo {
        log.debug { "Trying to rescue locally non-existing freetraining $freetrainingId for venue [$venueSlug]" }
        require(freetrainingRepo.selectById(freetrainingId) == null)
        val freetrainingDetail = uscApi.fetchFreetrainingDetails(freetrainingId).let(::adjustDate)
        val venue = ensureVenue(city, venueSlug, prefilledNotes)
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
