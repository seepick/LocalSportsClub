package seepick.localsportsclub.sync

import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.service.date.Clock

class CleanupPostSync(
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val clock: Clock,
    private val dispatcher: SyncerListenerDispatcher,
    private val progress: SyncProgress,
) {
    fun cleanup() {
        progress.onProgress("Cleanup")
        val today = clock.today()

        val activities = activityRepo.deleteBlanksBefore(today)
        dispatcher.dispatchOnActivityDbosDeleted(activities)

        val freetrainings = freetrainingRepo.deleteNonCheckedinBefore(today)
        dispatcher.dispatchOnFreetrainingDbosDeleted(freetrainings)
    }
}
