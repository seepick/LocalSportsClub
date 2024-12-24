package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.schedule.ScheduleApi
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo

class ScheduleSyncer(
    private val scheduleApi: ScheduleApi,
    private val activityRepo: ActivityRepo,
    private val dataSyncRescuer: DataSyncRescuer,
    private val dispatcher: SyncerListenerDispatcher,
) {
    private val log = logger {}

    suspend fun sync() {
        log.debug { "Syncing scheduled activities." }
        val scheduleRows = scheduleApi.fetchScheduleRows().associateBy { it.activityId }
        val localScheduled = activityRepo.selectAllScheduled().associateBy { it.id }

        val toMarkScheduledYes = scheduleRows.minus(localScheduled.keys)
        val toMarkScheduledNo = localScheduled.minus(scheduleRows.keys)

        updateAndDispatch(toMarkScheduledYes.values.toList(), true) { schedule ->
            activityRepo.selectById(schedule.activityId) ?: suspend {
                dataSyncRescuer.rescueActivity(schedule.activityId, schedule.venueSlug)
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
            require(activity.scheduled != toBeScheduled) { "Expected activity to be scheduled=${!toBeScheduled} ($activity)" }
            val updatedActivity = activity.copy(scheduled = toBeScheduled)
            activityRepo.update(updatedActivity)
            dispatcher.dispatchOnActivityDboUpdated(updatedActivity, ActivityFieldUpdate.Scheduled)
        }
    }
}
