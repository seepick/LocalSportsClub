package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.service.model.EntityType

class ScheduleSyncer(
    private val uscApi: UscApi,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val dataSyncRescuer: DataSyncRescuer,
    private val dispatcher: SyncerListenerDispatcher,
) {
    private val log = logger {}

    suspend fun sync() {
        log.debug { "Syncing scheduled activities." }
        val scheduleRows = uscApi.fetchScheduleRows()
        val scheduleActivities = scheduleRows.filter { it.entityType == EntityType.Activity }
            .associateBy { it.activityOrFreetrainingId }
        val scheduleFreetrainings = scheduleRows.filter { it.entityType == EntityType.Freetraining }
            .associateBy { it.activityOrFreetrainingId }
        val localScheduledActivities = activityRepo.selectAllBooked().associateBy { it.id }
        val localScheduledFreetrainings = freetrainingRepo.selectAllScheduled().associateBy { it.id }

        val activitiesYes = scheduleActivities.minus(localScheduledActivities.keys)
        val activitiesNo = localScheduledActivities.minus(scheduleActivities.keys)
        val freetrainingsYes = scheduleFreetrainings.minus(localScheduledFreetrainings.keys)
        val freetrainingsNo = localScheduledFreetrainings.minus(scheduleFreetrainings.keys)

        updateAndDispatchActivities(activitiesYes.values.toList(), toBeScheduled = true) { schedule ->
            activityRepo.selectById(schedule.activityOrFreetrainingId) ?: suspend {
                dataSyncRescuer.fetchInsertAndDispatchActivity(
                    schedule.activityOrFreetrainingId,
                    schedule.venueSlug,
                    "[SYNC] refetch due to missing from booked activity"
                )
            }()
        }
        updateAndDispatchActivities(activitiesNo.values.toList(), toBeScheduled = false) { it }

        updateAndDispatchFreetrainings(freetrainingsYes.values.toList(), toBeScheduled = true) { schedule ->
            freetrainingRepo.selectById(schedule.activityOrFreetrainingId) ?: suspend {
                dataSyncRescuer.fetchInsertAndDispatchFreetraining(
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
        toBeScheduled: Boolean,
        extractor: suspend (T) -> ActivityDbo
    ) {
        log.debug { "Marking ${activities.size} activities as booked=$toBeScheduled" }
        activities.forEach {
            val activity = extractor(it)
            require(activity.isBooked != toBeScheduled) { "Expected activity to be scheduled=${!toBeScheduled} ($activity)" }
            val updatedActivity = activity.copy(isBooked = toBeScheduled)
            activityRepo.update(updatedActivity)
            dispatcher.dispatchOnActivityDboUpdated(updatedActivity, ActivityFieldUpdate.IsBooked)
        }
    }

    private suspend fun <T> updateAndDispatchFreetrainings(
        freetrainings: List<T>,
        toBeScheduled: Boolean,
        extractor: suspend (T) -> FreetrainingDbo
    ) {
        log.debug { "Marking ${freetrainings.size} freetrainings as scheduled=$toBeScheduled" }
        freetrainings.forEach {
            val freetraining = extractor(it)
            require(freetraining.isScheduled != toBeScheduled) { "Expected freetraining to be scheduled=${!toBeScheduled} ($freetraining)" }
            val updatedFreetraining = freetraining.copy(isScheduled = toBeScheduled)
            freetrainingRepo.update(updatedFreetraining)
            dispatcher.dispatchOnFreetrainingDboUpdated(updatedFreetraining, FreetrainingFieldUpdate.IsScheduled)
        }
    }
}
