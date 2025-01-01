package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.view.shared.ScreenItem

class Activity(
    val id: Int,
    override val venue: SimpleVenue,
    val name: String,
    val category: String, // aka disciplines/facilities
    val dateTimeRange: DateTimeRange,
    val teacher: String?,
    spotsLeft: Int,
    isBooked: Boolean,
    wasCheckedin: Boolean,
) : ScreenItem {
    val nameWithTeacherIfPresent = if (teacher == null) name else "$name /$teacher"
    var spotsLeft: Int by mutableStateOf(spotsLeft)

    var isBooked: Boolean by mutableStateOf(isBooked)
    var wasCheckedin by mutableStateOf(wasCheckedin)

    override fun toString() = "Activity[id=$id, name=$name, venue.slug=${venue.slug}]"
}
