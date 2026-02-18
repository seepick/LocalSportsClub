package seepick.localsportsclub.service

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.activity.ActivityDetails
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.SyncProgress
import seepick.localsportsclub.sync.SyncerListenerDispatcher
import java.util.concurrent.atomic.AtomicInteger

class ActivityDetailService(
    private val api: UscApi,
    private val activityRepo: ActivityRepo,
    private val dispatcher: SyncerListenerDispatcher,
    private val progress: SyncProgress,
) {
    private val log = logger {}

    suspend fun syncSingle(activityId: Int) {
        log.debug { "syncSingle(activityId=$activityId)" }

        val details = api.fetchActivityDetails(activityId)
        updateDboAndDispatch(activityId, details)
    }

    suspend fun syncBulk(activityIds: List<Int>) {
        val activitiesDone = AtomicInteger(0)

        val idAndDetails = workParallel(5, activityIds) { activityId ->
            val details = api.fetchActivityDetails(activityId)
            val soFarDone = activitiesDone.incrementAndGet()
            if (soFarDone % 10 == 0) {
                val percentageDone = (activitiesDone.get() * 100.0 / activityIds.size).toInt()
                progress.onProgress("Auto-Sync", "${percentageDone}%")
            }
            activityId to details
        }
        idAndDetails.forEach { (id, details) ->
            updateDboAndDispatch(id, details)
        }
    }

    private fun updateDboAndDispatch(activityId: Int, details: ActivityDetails) {
        val oldActivityDbo = activityRepo.selectById(activityId)!!
        val newActivityDbo = oldActivityDbo.copy(
            teacher = details.teacher,
            description = details.description,
            spotsLeft = details.spotsLeft,
        )
        activityRepo.update(newActivityDbo)

        if (oldActivityDbo.teacher != details.teacher) {
            dispatcher.dispatchOnActivityDboUpdated(activityDbo = newActivityDbo, field = ActivityFieldUpdate.Teacher)
        }
        if (oldActivityDbo.description != details.description) {
            dispatcher.dispatchOnActivityDboUpdated(
                activityDbo = newActivityDbo,
                field = ActivityFieldUpdate.Description
            )
        }
        if (oldActivityDbo.spotsLeft != details.spotsLeft) {
            dispatcher.dispatchOnActivityDboUpdated(activityDbo = newActivityDbo, field = ActivityFieldUpdate.SpotsLeft)
        }
    }
}
