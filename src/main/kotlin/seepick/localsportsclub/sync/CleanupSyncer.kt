package seepick.localsportsclub.sync

import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.service.date.Clock

class CleanupSyncer(
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val clock: Clock,
    private val dispatcher: SyncerListenerDispatcher,
) {
    fun sync() {
        val today = clock.today()

        val activities = activityRepo.deleteBlanksBefore(today)
        dispatcher.dispatchOnActivityDbosDeleted(activities)

        val freetrainings = freetrainingRepo.deleteNonCheckedinBefore(today)
        dispatcher.dispatchOnFreetrainingDbosDeleted(freetrainings)
    }
}
