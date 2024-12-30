package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.view.common.ScreenItem

class Activity(
    val id: Int,
    override val venue: SimpleVenue,
    val name: String,
    val category: String, // aka disciplines/facilities
    val dateTimeRange: DateTimeRange,
    spotsLeft: Int,
    isBooked: Boolean,
    wasCheckedin: Boolean,
) : ScreenItem {
    var spotsLeft: Int by mutableStateOf(spotsLeft)
    var isBooked: Boolean by mutableStateOf(isBooked)
    var wasCheckedin: Boolean by mutableStateOf(wasCheckedin)

    override fun toString() = "Activity[id=$id, name=$name, venue.slug=${venue.slug}]"
}

