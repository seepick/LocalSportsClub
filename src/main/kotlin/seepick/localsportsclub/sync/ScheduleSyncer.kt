package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo

class ScheduleSyncer(
    private val uscApi: UscApi,
    private val activityRepo: ActivityRepo,
    private val dataSyncRescuer: DataSyncRescuer,
    private val dispatcher: SyncerListenerDispatcher,
) {
    private val log = logger {}

    suspend fun sync() {
        log.debug { "Syncing scheduled activities." }
        val scheduleRows = uscApi.fetchScheduleRows().associateBy { it.activityId }
        val localScheduled = activityRepo.selectAllBooked().associateBy { it.id }

        val toMarkScheduledYes = scheduleRows.minus(localScheduled.keys)
        val toMarkScheduledNo = localScheduled.minus(scheduleRows.keys)

        updateAndDispatch(toMarkScheduledYes.values.toList(), true) { schedule ->
            activityRepo.selectById(schedule.activityId) ?: suspend {
                dataSyncRescuer.rescueActivity(
                    schedule.activityId,
                    schedule.venueSlug,
                    "[SYNC] refetch due to missing from booked activity"
                )
            }()
        }
        updateAndDispatch(toMarkScheduledNo.values.toList(), false) { it }
    }

    private suspend fun <T> updateAndDispatch(
        activities: List<T>,
        toBeScheduled: Boolean,
        extractor: suspend (T) -> ActivityDbo
    ) {
        activities.forEach {
            val activity = extractor(it)
            require(activity.isBooked != toBeScheduled) { "Expected activity to be scheduled=${!toBeScheduled} ($activity)" }
            val updatedActivity = activity.copy(isBooked = toBeScheduled)
            activityRepo.update(updatedActivity)
            dispatcher.dispatchOnActivityDboUpdated(updatedActivity, ActivityFieldUpdate.IsBooked)
        }
    }
}
