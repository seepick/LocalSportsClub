package seepick.localsportsclub.sync

import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.VenueDbo

abstract class TestSyncerListener : SyncerListener {
    override fun onVenueDbosAdded(addedVenues: List<VenueDbo>) {
        error("Unexpected call to onVenueDbosAdded! $addedVenues")
    }

    override fun onVenueDbosMarkedDeleted(venueDbos: List<VenueDbo>) {
        error("Unexpected call to onVenueDbosMarkedDeleted! $venueDbos")
    }

    override fun onVenueDbosMarkedUndeleted(venueDbos: List<VenueDbo>) {
        error("Unexpected call to onVenueDbosMarkedUndeleted! $venueDbos")
    }

    override fun onActivityDbosAdded(addedActivities: List<ActivityDbo>) {
        error("Unexpected call to onActivityDboAdded!")
    }

    override fun onActivityDboUpdated(updatedActivity: ActivityDbo, field: ActivityFieldUpdate) {
        error("Unexpected call to onActivityDboUpdated! $field $updatedActivity")
    }

    override fun onFreetrainingDbosAdded(addedFreetrainings: List<FreetrainingDbo>) {
        error("Unexpected call to onFreetrainingDboAdded! $addedFreetrainings")
    }

    override fun onFreetrainingDboUpdated(updatedFreetraining: FreetrainingDbo, field: FreetrainingFieldUpdate) {
        error("Unexpected call to onFreetrainingDboUpdated! $field $updatedFreetraining")
    }

    override fun onActivityDbosDeleted(deletedActivities: List<ActivityDbo>) {
        error("Unexpected call to onActivityDbosDeleted! $deletedActivities")
    }

    override fun onFreetrainingDbosDeleted(deletedFreetrainings: List<FreetrainingDbo>) {
        error("Unexpected call to onFreetrainingDbosDeleted! $deletedFreetrainings")
    }
}
