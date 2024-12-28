package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.prettyPrint
import seepick.localsportsclub.view.LscIcons

@Composable
fun SimpleActivitiesTable(activities: List<Activity>, currentYear: Int) {
    if (activities.isEmpty()) {
        Text("No activities.")
    } else {
        Text("Activities:")
        LazyColumn {
            items(activities) { activity ->
                Row {
                    if (activity.isBooked) {
                        Text(text = LscIcons.booked)
                    }
                    if (activity.wasCheckedin) {
                        Text(text = LscIcons.checkedin)
                    }
                    Text(text = "${activity.name} - ${activity.dateTimeRange.prettyPrint(currentYear)}")
                }
            }
        }
    }
}
