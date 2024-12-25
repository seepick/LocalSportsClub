package seepick.localsportsclub.sync

import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.service.Clock

class CleanupSyncer(
    private val activityRepo: ActivityRepo,
    private val clock: Clock,
) {
    fun sync() {
        activityRepo.deleteNonBookedNonCheckedinBefore(clock.today())
    }
}
