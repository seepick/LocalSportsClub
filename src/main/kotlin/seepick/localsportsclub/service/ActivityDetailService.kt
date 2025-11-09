package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.PhpSessionProvider
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.SyncerListenerDispatcher

class ActivityDetailService(
    private val api: UscApi,
    private val activityRepo: ActivityRepo,
    private val phpSessionProvider: PhpSessionProvider,
    private val dispatcher: SyncerListenerDispatcher,
) {
    private val log = logger {}

    suspend fun syncSingle(activityId: Int) {
        log.debug { "syncSingle(activityId=$activityId)" }

        val oldActivityDbo = activityRepo.selectById(activityId)!!
        val session = phpSessionProvider.provide()
        val details = api.fetchActivityDetails(session, activityId)
        val newActivityDbo = oldActivityDbo.copy(teacher = details.teacher, description = details.description)
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
    }
}
