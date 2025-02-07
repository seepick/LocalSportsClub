package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.PhpSessionId
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.EntityType
import seepick.localsportsclub.service.model.FreetrainingState

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

    suspend fun sync(session: PhpSessionId, city: City) {
        log.debug { "Syncing scheduled activities." }
        progress.onProgress("Schedule")
        val scheduleRows = uscApi.fetchScheduleRows(session)
        val scheduleActivities = scheduleRows.filter { it.entityType == EntityType.Activity }
            .associateBy { it.activityOrFreetrainingId }
        val scheduleFreetrainings = scheduleRows.filter { it.entityType == EntityType.Freetraining }
            .associateBy { it.activityOrFreetrainingId }
        val localBookedActivities = activityRepo.selectAllBooked(city.id).associateBy { it.id }
        val localScheduledFreetrainings = freetrainingRepo.selectAllScheduled(city.id).associateBy { it.id }

        val activitiesYes = scheduleActivities.minus(localBookedActivities.keys)
        val activitiesNo = localBookedActivities.minus(scheduleActivities.keys)
        val freetrainingsYes = scheduleFreetrainings.minus(localScheduledFreetrainings.keys)
        val freetrainingsNo = localScheduledFreetrainings.minus(scheduleFreetrainings.keys)

        updateAndDispatchActivities(activitiesYes.values.toList(), toBeBooked = true) { schedule ->
            activityRepo.selectById(schedule.activityOrFreetrainingId) ?: suspend {
                dataSyncRescuer.fetchInsertAndDispatchActivity(
                    session,
                    city,
                    schedule.activityOrFreetrainingId,
                    schedule.venueSlug,
                    "[SYNC] refetch due to missing from booked activity"
                )
            }()
        }
        updateAndDispatchActivities(activitiesNo.values.toList(), toBeBooked = false) { it }

        updateAndDispatchFreetrainings(freetrainingsYes.values.toList(), toBeScheduled = true) { schedule ->
            freetrainingRepo.selectById(schedule.activityOrFreetrainingId) ?: suspend {
                dataSyncRescuer.fetchInsertAndDispatchFreetraining(
                    session,
                    city,
                    schedule.activityOrFreetrainingId,
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
        extractor: suspend (T) -> ActivityDbo
    ) {
        log.debug { "Marking ${activities.size} activities as booked=$toBeBooked" }
        val targetState = if (toBeBooked) ActivityState.Booked else ActivityState.Blank
        activities.forEach {
            val activity = extractor(it)
            require(activity.state != targetState) { "Expected activity state not to be $targetState: $activity" }
            val updatedActivity = activity.copy(state = targetState)
            activityRepo.update(updatedActivity)
            dispatcher.dispatchOnActivityDboUpdated(
                updatedActivity,
                ActivityFieldUpdate.State(oldState = activity.state)
            )
        }
    }

    private suspend fun <T> updateAndDispatchFreetrainings(
        freetrainings: List<T>,
        toBeScheduled: Boolean,
        extractor: suspend (T) -> FreetrainingDbo
    ) {
        log.debug { "Marking ${freetrainings.size} freetrainings as scheduled=$toBeScheduled" }
        val targetState = if (toBeScheduled) FreetrainingState.Scheduled else FreetrainingState.Blank
        freetrainings.forEach {
            val freetraining = extractor(it)
            require(freetraining.state != targetState) { "Expected freetraining state not to be $targetState: $freetraining" }
            val updatedFreetraining = freetraining.copy(state = targetState)
            freetrainingRepo.update(updatedFreetraining)
            dispatcher.dispatchOnFreetrainingDboUpdated(updatedFreetraining, FreetrainingFieldUpdate.State)
        }
    }
}
