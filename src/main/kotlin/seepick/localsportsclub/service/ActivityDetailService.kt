package seepick.localsportsclub.service

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.activity.ActivityDetails
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.SyncProgress
import seepick.localsportsclub.sync.SyncerListenerDispatcher
import java.util.concurrent.atomic.AtomicInteger

class ActivityDetailService(
    private val api: UscApi,
    private val activityRepo: ActivityRepo,
    private val dispatcher: SyncerListenerDispatcher,
    private val progress: SyncProgress,
    private val enrichers: List<ActivityEnricher>,
) {
    private val log = logger {}

    init {
        log.debug { "Initialized with ${enrichers.size} enrichers." }
    }

    suspend fun syncSingle(activity: Activity) {
        syncSingle(activityRepo.selectById(activity.id) ?: error("Activity with id ${activity.id} not found"))
    }

    suspend fun syncSingle(activity: ActivityDbo) {
        log.debug { "syncSingle(activity=$activity)" }
        syncBulk(listOf(activity))
    }

    suspend fun syncBulk(activities: List<ActivityDbo>) {
        log.debug { "syncBulk(activities.size=${activities.size})" }
        val activitiesDone = AtomicInteger(0)
        val activitiesCount = activities.size
        val originalDboDetails = workParallel(minOf(5, activities.size), activities) { dbo ->
            fetchDetails(dbo, activitiesDone, activitiesCount)
        }
        val enrichedDboDetails = enrichers.fold(originalDboDetails) { acc, enricher ->
            enricher.enrich(acc)
        }
        enrichedDboDetails.forEach { (dbo, details) ->
            updateDboAndDispatch(dbo, details)
        }
    }

    private suspend fun fetchDetails(
        dbo: ActivityDbo,
        activitiesDone: AtomicInteger,
        activitiesCount: Int,
    ): Pair<ActivityDbo, ActivityDetails> {
        val details = api.fetchActivityDetails(dbo.id)
        val soFarDone = activitiesDone.incrementAndGet()
        if (soFarDone % 10 == 0) {
            val percentageDone = (activitiesDone.get() * 100.0 / activitiesCount).toInt()
            progress.onProgress("Auto-Sync", "${percentageDone}%")
        }
        return dbo to details
    }

    private fun updateDboAndDispatch(activity: ActivityDbo, details: ActivityDetails) {
        val oldActivityDbo = activityRepo.selectById(activity.id)!! // could also just use `activity`?!
        val newActivityDbo = oldActivityDbo.copy(
            teacher = details.teacher ?: activity.teacher,
            description = details.description,
            spotsLeft = details.spotsLeft,
            cancellationLimit = details.cancellationDateLimit,
        )
        activityRepo.update(newActivityDbo)

        if (oldActivityDbo.teacher != details.teacher) {
            dispatcher.dispatchOnActivityDboUpdated(
                updatedActivity = newActivityDbo,
                field = ActivityFieldUpdate.Teacher
            )
        }
        if (oldActivityDbo.description != details.description) {
            dispatcher.dispatchOnActivityDboUpdated(
                updatedActivity = newActivityDbo,
                field = ActivityFieldUpdate.Description
            )
        }
        if (oldActivityDbo.spotsLeft != details.spotsLeft) {
            dispatcher.dispatchOnActivityDboUpdated(
                updatedActivity = newActivityDbo,
                field = ActivityFieldUpdate.SpotsLeft
            )
        }
        if (oldActivityDbo.cancellationLimit != details.cancellationDateLimit) {
            dispatcher.dispatchOnActivityDboUpdated(
                updatedActivity = newActivityDbo,
                field = ActivityFieldUpdate.CancellationLimit
            )
        }
    }
}
