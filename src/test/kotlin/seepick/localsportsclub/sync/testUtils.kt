package seepick.localsportsclub.sync

import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.VenueDbo

abstract class TestSyncerListener : SyncerListener {
    override fun onVenueDboAdded(venueDbo: VenueDbo) {
        error("Unexpected call to onVenueDboAdded!")
    }

    override fun onActivityDboAdded(activityDbo: ActivityDbo) {
        error("Unexpected call to onActivityDboAdded!")
    }

    override fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        error("Unexpected call to onActivityDboUpdated!")
    }

    override fun onFreetrainingDboAdded(freetrainingDbo: FreetrainingDbo) {
        error("Unexpected call to onFreetrainingDboAdded")
    }
}
