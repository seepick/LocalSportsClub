package seepick.localsportsclub.sync.domain

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.model.City
import com.github.seepick.uscclient.schedule.BookedActivity
import com.github.seepick.uscclient.schedule.ScheduledFreetraining
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import lsc.repo.ActivityDbo
import lsc.repo.ActivityRepo
import lsc.repo.ActivityStateDbo
import lsc.repo.FreetrainingDbo
import lsc.repo.FreetrainingRepo
import lsc.repo.FreetrainingStateDbo
import seepick.localsportsclub.service.model.toActivityState
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.FreetrainingFieldUpdate
import seepick.localsportsclub.sync.SyncProgress
import seepick.localsportsclub.sync.SyncerListenerDispatcher

/**
 * For booked activities and scheduled freetrainings.
 */
class ScheduleSyncer(
    private val uscApi: UscApi,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val dataSyncRescuer: DataSyncRescuer,
    private val dispatcher: SyncerListenerDispatcher,
    private val progress: SyncProgress,
) {
    private val log = logger {}

    suspend fun sync(city: City) {
        log.debug { "Syncing scheduled activities." }
        progress.onProgress("Schedule")
        val scheduleds = uscApi.fetchScheduleds()
        val scheduleActivities = scheduleds.filterIsInstance<BookedActivity>().associateBy { it.id }
        val scheduleFreetrainings = scheduleds.filterIsInstance<ScheduledFreetraining>().associateBy { it.id }
        val localBookedActivities = activityRepo.selectAllBooked(city.id).associateBy { it.id }
        val localScheduledFreetrainings = freetrainingRepo.selectAllScheduled(city.id).associateBy { it.id }

        val activitiesYes = scheduleActivities.minus(localBookedActivities.keys)
        val activitiesNo = localBookedActivities.minus(scheduleActivities.keys)
        val freetrainingsYes = scheduleFreetrainings.minus(localScheduledFreetrainings.keys)
        val freetrainingsNo = localScheduledFreetrainings.minus(scheduleFreetrainings.keys)

        updateAndDispatchActivities(activitiesYes.values.toList(), toBeBooked = true) { bookedActivity ->
            activityRepo.selectById(bookedActivity.activityId) ?: suspend {
                dataSyncRescuer.fetchInsertAndDispatchActivity(
                    city = city,
                    activityId = bookedActivity.activityId,
                    venueSlug = bookedActivity.venueSlug,
                    prefilledVenueNotes = "[SYNC] refetch due to missing from booked activity"
                )
            }()
        }
        updateAndDispatchActivities(activitiesNo.values.toList(), toBeBooked = false) { it }

        updateAndDispatchFreetrainings(freetrainingsYes.values.toList(), toBeScheduled = true) { schedule ->
            freetrainingRepo.selectById(schedule.freetrainingId) ?: suspend {
                dataSyncRescuer.fetchInsertAndDispatchFreetraining(
                    city,
                    schedule.freetrainingId,
                    schedule.venueSlug,
                    "[SYNC] refetch due to missing from scheduled freetraining"
                )
            }()
        }
        updateAndDispatchFreetrainings(freetrainingsNo.values.toList(), toBeScheduled = false) { it }
    }

    private suspend fun <T> updateAndDispatchActivities(
        activities: List<T>,
        toBeBooked: Boolean,
        extractor: suspend (T) -> ActivityDbo,
    ) {
        log.debug { "Marking ${activities.size} activities as booked=$toBeBooked" }
        val targetState = if (toBeBooked) ActivityStateDbo.Booked else ActivityStateDbo.Blank
        activities.forEach {
            val activity = extractor(it)
            require(activity.state != targetState) { "Expected activity state not to be $targetState: $activity" }
            val updatedActivity = activity.copy(state = targetState)
            activityRepo.update(updatedActivity)
            dispatcher.dispatchOnActivityDboUpdated(
                updatedActivity,
                ActivityFieldUpdate.State(oldState = activity.state.toActivityState())
            )
        }
    }

    private suspend fun <T> updateAndDispatchFreetrainings(
        freetrainings: List<T>,
        toBeScheduled: Boolean,
        extractor: suspend (T) -> FreetrainingDbo,
    ) {
        log.debug { "Marking ${freetrainings.size} freetrainings as scheduled=$toBeScheduled" }
        val targetState = if (toBeScheduled) FreetrainingStateDbo.Scheduled else FreetrainingStateDbo.Blank
        freetrainings.forEach {
            val freetraining = extractor(it)
            require(freetraining.state != targetState) { "Expected freetraining state not to be $targetState: $freetraining" }
            val updatedFreetraining = freetraining.copy(state = targetState)
            freetrainingRepo.update(updatedFreetraining)
            dispatcher.dispatchOnFreetrainingDboUpdated(updatedFreetraining, FreetrainingFieldUpdate.State)
        }
    }
}
