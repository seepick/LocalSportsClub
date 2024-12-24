package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.service.DateTimeRange

class Activity(
    val id: Int,
    val venue: SimpleVenue,
    val name: String,
    val category: String, // aka disciplines/facilities
    val dateTimeRange: DateTimeRange,
    spotsLeft: Int,
    scheduled: Boolean,
) {
    var spotsLeft: Int by mutableStateOf(spotsLeft)
    var scheduled: Boolean by mutableStateOf(scheduled)

    override fun toString() = "Activity[id=$id, name=$name, venue.slug=${venue.slug}]"
}

