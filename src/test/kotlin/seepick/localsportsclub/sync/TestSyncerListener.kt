package seepick.localsportsclub.sync

import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.VenueDbo

abstract class TestSyncerListener : SyncerListener {

    override fun onVenueDbosAdded(venueDbos: List<VenueDbo>) {
        error("Unexpected call to onVenueDbosAdded!")
    }

    override fun onActivityDbosAdded(activityDbos: List<ActivityDbo>) {
        error("Unexpected call to onActivityDboAdded!")
    }

    override fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        error("Unexpected call to onActivityDboUpdated!")
    }

    override fun onFreetrainingDbosAdded(freetrainingDbos: List<FreetrainingDbo>) {
        error("Unexpected call to onFreetrainingDboAdded")
    }

    override fun onFreetrainingDboUpdated(freetrainingDbo: FreetrainingDbo, field: FreetrainingFieldUpdate) {
        error("Unexpected call to onFreetrainingDboUpdated")
    }

    override fun onActivityDbosDeleted(activityDbos: List<ActivityDbo>) {
        error("Unexpected call to onActivityDbosDeleted")
    }

    override fun onFreetrainingDbosDeleted(freetrainingDbos: List<FreetrainingDbo>) {
        error("Unexpected call to onFreetrainingDbosDeleted")
    }
}
