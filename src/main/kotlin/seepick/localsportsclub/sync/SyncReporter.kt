package seepick.localsportsclub.sync

import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState

data class SyncReport(
    var venuesAdded: Int = 0,
    var venuesMarkedDeleted: Int = 0,

    var activitiesAdded: Int = 0,
    var activitiesBooked: Int = 0,
    var activitiesCheckedin: Int = 0,
    var activitiesNoshow: Int = 0,

    var freetrainingsAdded: Int = 0,
    var freetrainingsScheduled: Int = 0,
    var freetrainingsCheckedin: Int = 0,
) {
    fun buildMessage(): String? {
        val string = buildString {
            if (venuesAdded != 0 || venuesMarkedDeleted != 0) {
                append("Venues +$venuesAdded/-$venuesMarkedDeleted; ")
            }
            if (activitiesAdded != 0 || freetrainingsAdded != 0) {
                append("+$activitiesAdded Act. / +$freetrainingsAdded Free.; ")
            }
            // TODO reuse known Lsc.icons
            if (activitiesBooked != 0 || freetrainingsScheduled != 0) {
                append("Booked $activitiesBooked Act. / $freetrainingsScheduled Free.; ")
            }
            if (activitiesCheckedin != 0 || freetrainingsCheckedin != 0) {
                // TODO only show for != 0
                append("Checked-in $activitiesCheckedin Act. / $freetrainingsCheckedin Free.; ")
            }
            // TODO highlight no-shows with red color
            if (activitiesNoshow != 0) {
                append("+$activitiesNoshow no-shows; ")
            }
        }
        return if (string.isEmpty()) null else string.substring(0, string.length - 2)
    }
}

class SyncReporter : SyncerListener {
    var report = SyncReport()
        private set

    override fun alsoRegisterForBooking(): Boolean = false

    fun clear() {
        report = SyncReport()
    }

    override fun onVenueDbosAdded(venueDbos: List<VenueDbo>) {
        report.venuesAdded++
    }

    override fun onVenueDbosMarkedDeleted(venueDbos: List<VenueDbo>) {
        report.venuesMarkedDeleted++
    }

    override fun onActivityDbosAdded(activityDbos: List<ActivityDbo>) {
        report.activitiesAdded++
    }

    override fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        if (field == ActivityFieldUpdate.State) {
            when (activityDbo.state) {
                ActivityState.Blank -> {} // ignore
                ActivityState.Booked -> report.activitiesBooked++
                ActivityState.Checkedin -> report.activitiesCheckedin++
                ActivityState.Noshow -> report.activitiesNoshow++
            }
        }
    }

    override fun onFreetrainingDbosAdded(freetrainingDbos: List<FreetrainingDbo>) {
        report.freetrainingsAdded++
    }

    override fun onFreetrainingDboUpdated(freetrainingDbo: FreetrainingDbo, field: FreetrainingFieldUpdate) {
        if (field == FreetrainingFieldUpdate.State) {
            when (freetrainingDbo.state) {
                FreetrainingState.Blank -> {} // ignore
                FreetrainingState.Scheduled -> report.freetrainingsScheduled++
                FreetrainingState.Checkedin -> report.freetrainingsCheckedin++
            }
        }
    }

    override fun onActivityDbosDeleted(activityDbos: List<ActivityDbo>) {
        // no op
    }

    override fun onFreetrainingDbosDeleted(freetrainingDbos: List<FreetrainingDbo>) {
        // no op
    }
}
