package seepick.localsportsclub.sync

import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.VenueDbo

abstract class TestSyncerListener : SyncerListener {
    override fun onVenueDbosAdded(venueDbos: List<VenueDbo>) {
        error("Unexpected call to onVenueDbosAdded! $venueDbos")
    }

    override fun onVenueDbosMarkedDeleted(venueDbos: List<VenueDbo>) {
        error("Unexpected call to onVenueDbosMarkedDeleted! $venueDbos")
    }

    override fun onActivityDbosAdded(activityDbos: List<ActivityDbo>) {
        error("Unexpected call to onActivityDboAdded!")
    }

    override fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        error("Unexpected call to onActivityDboUpdated! $field $activityDbo")
    }

    override fun onFreetrainingDbosAdded(freetrainingDbos: List<FreetrainingDbo>) {
        error("Unexpected call to onFreetrainingDboAdded! $freetrainingDbos")
    }

    override fun onFreetrainingDboUpdated(freetrainingDbo: FreetrainingDbo, field: FreetrainingFieldUpdate) {
        error("Unexpected call to onFreetrainingDboUpdated! $field $freetrainingDbo")
    }

    override fun onActivityDbosDeleted(activityDbos: List<ActivityDbo>) {
        error("Unexpected call to onActivityDbosDeleted! $activityDbos")
    }

    override fun onFreetrainingDbosDeleted(freetrainingDbos: List<FreetrainingDbo>) {
        error("Unexpected call to onFreetrainingDbosDeleted! $freetrainingDbos")
    }
}
