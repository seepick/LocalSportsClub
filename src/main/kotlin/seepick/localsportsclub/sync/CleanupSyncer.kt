package seepick.localsportsclub.sync

import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.service.date.Clock

class CleanupSyncer(
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val clock: Clock,
) {
    fun sync() {
        val today = clock.today()
        activityRepo.deleteNonBookedNonCheckedinBefore(today)
        freetrainingRepo.deleteNonCheckedinBefore(today)
    }
}
