package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.schedule.ScheduleApi
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo

class ScheduleSyncer(
    private val scheduleApi: ScheduleApi,
    private val activityRepo: ActivityRepo,
    private val dispatcher: SyncerListenerDispatcher,
) {
    private val log = logger {}

    suspend fun sync() {
        log.debug { "Syncing scheduled activities." }
        val remoteScheduledIds = scheduleApi.fetchActivityIds()
        val localScheduled = activityRepo.selectAllScheduled().associateBy { it.id }

        val toMarkScheduledYes = remoteScheduledIds.minus(localScheduled.keys)
        val toMarkScheduledNo = localScheduled.minus(remoteScheduledIds.toSet())

        updateAndDispatch(toMarkScheduledYes, true) { activityRepo.selectById(it) }
        updateAndDispatch(toMarkScheduledNo.values.toList(), false) { it }
    }

    private fun <T> updateAndDispatch(activities: List<T>, toBeScheduled: Boolean, extractor: (T) -> ActivityDbo) {
        activities.forEach {
            val activity = extractor(it)
            require(activity.scheduled != toBeScheduled)
            val updatedActivity = activity.copy(scheduled = toBeScheduled)
            activityRepo.update(updatedActivity)
            dispatcher.dispatchOnActivityDboUpdated(updatedActivity, ActivityFieldUpdate.Scheduled)
        }
    }
}
