package seepick.localsportsclub.sync

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState

private data class ReportEntry(
    val tokens: List<ReportToken>
)

private sealed interface ReportToken {
    data class Text(val text: String) : ReportToken
    data class ColoredText(val text: String, val color: Color) : ReportToken
}

data class SyncReport(
    var venuesAdded: Int = 0,
    var venuesMarkedDeleted: Int = 0,

    var activitiesAdded: Int = 0,
    var activitiesBooked: Int = 0,
    var activitiesCheckedin: Int = 0,
    var activitiesNoshow: Int = 0,
    var activitiesCancelledLate: Int = 0,


    var freetrainingsAdded: Int = 0,
    var freetrainingsScheduled: Int = 0,
    var freetrainingsCheckedin: Int = 0,
) {
    fun buildContent(): @Composable () -> Unit {
//        venuesAdded += 3
//        venuesMarkedDeleted += 2
//        activitiesAdded += 4_124
//        activitiesBooked += 4
//        freetrainingsAdded += 132
//        freetrainingsScheduled += 1
//        activitiesCheckedin += 2
//        activitiesNoshow += 1
//        activitiesCancelledLate += 2

        val entries = buildList {
            if (venuesAdded != 0 || venuesMarkedDeleted != 0) {
                add(ReportEntry(buildList {
                    add(ReportToken.Text("Venues "))
                    if (venuesAdded != 0) add(ReportToken.ColoredText("+$venuesAdded", Color.Green))
                    if (venuesAdded != 0 && venuesMarkedDeleted != 0) add(ReportToken.Text("/"))
                    if (venuesMarkedDeleted != 0) add(ReportToken.ColoredText("-$venuesMarkedDeleted", Color.Gray))
                }))
            }
            if (activitiesAdded != 0) {
                add(ReportEntry(buildList {
                    add(ReportToken.Text("Activities "))
                    add(ReportToken.ColoredText("+$activitiesAdded", Color.Green))
                }))
            }
            if (freetrainingsAdded != 0) {
                add(ReportEntry(buildList {
                    add(ReportToken.Text("Freetrainings "))
                    add(ReportToken.ColoredText("+$freetrainingsAdded", Color.Green))
                }))
            }
            val totalBooked = activitiesBooked + freetrainingsScheduled
            if (totalBooked != 0) {
                add(ReportEntry(buildList {
                    add(ReportToken.Text("${Lsc.icons.reservedEmoji} Booked "))
                    add(ReportToken.ColoredText("+${totalBooked}", Color.Green))
                }))
            }
            val totalCheckedin = activitiesCheckedin + freetrainingsCheckedin
            if (totalCheckedin != 0) {
                add(ReportEntry(buildList {
                    add(ReportToken.Text("${Lsc.icons.checkedinEmoji} Checked-In "))
                    add(ReportToken.ColoredText("+$totalCheckedin", Color.Green))
                }))
            }
            if (activitiesNoshow != 0 || activitiesCancelledLate != 0) {
                add(ReportEntry(buildList {
                    if (activitiesNoshow != 0) {
                        add(ReportToken.Text(Lsc.icons.noshowEmoji))
                        add(ReportToken.Text(" No-Show "))
                        add(ReportToken.ColoredText("$activitiesNoshow", Color.Red))
                    }
                    if (activitiesNoshow != 0 && activitiesCancelledLate != 0) add(ReportToken.Text(" / "))
                    if (activitiesCancelledLate != 0) {
                        add(ReportToken.Text(Lsc.icons.cancelledLateEmoji))
                        add(ReportToken.Text(" Cancelled-Late "))
                        add(ReportToken.ColoredText("$activitiesCancelledLate", Color.Red))
                    }
                }))
            }
        }

        return {
            Column {
                Text("Finished synchronizing data 🔄✅")
                if (venuesAdded != 0 || venuesMarkedDeleted != 0) {
                    Text(
                        fontSize = 11.sp,
                        text = buildAnnotatedString {
                            if (entries.isEmpty()) {
                                append("Everything was up-to-date, nothing was changed.")
                            }
                            entries.forEachIndexed { index, entry ->
                                if (index != 0) {
                                    append(" ● ")
                                }
                                entry.tokens.forEach { token ->
                                    when (token) {
                                        is ReportToken.ColoredText -> withStyle(style = SpanStyle(color = token.color)) {
                                            append(token.text)
                                        }

                                        is ReportToken.Text -> append(token.text)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
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
        report.venuesAdded += venueDbos.size
    }

    override fun onVenueDbosMarkedDeleted(venueDbos: List<VenueDbo>) {
        report.venuesMarkedDeleted += venueDbos.size
    }

    override fun onActivityDbosAdded(activityDbos: List<ActivityDbo>) {
        report.activitiesAdded += activityDbos.size
    }

    override fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        if (field is ActivityFieldUpdate.State) {
            when (activityDbo.state) {
                ActivityState.Blank -> {} // ignore
                ActivityState.Booked -> report.activitiesBooked++
                ActivityState.Checkedin -> report.activitiesCheckedin++
                ActivityState.Noshow -> report.activitiesNoshow++
                ActivityState.CancelledLate -> report.activitiesCancelledLate++
            }
        }
    }

    override fun onFreetrainingDbosAdded(freetrainingDbos: List<FreetrainingDbo>) {
        report.freetrainingsAdded += freetrainingDbos.size
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
