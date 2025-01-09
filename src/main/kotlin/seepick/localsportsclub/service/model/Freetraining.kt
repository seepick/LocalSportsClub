package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.view.shared.HasVenue
import java.time.LocalDate

class Freetraining(
    val id: Int,
    override val venue: Venue,
    val name: String,
    val category: String,
    val date: LocalDate,
    isScheduled: Boolean,
    wasCheckedin: Boolean,
) : HasVenue {
    var isScheduled: Boolean by mutableStateOf(isScheduled)
    var wasCheckedin: Boolean by mutableStateOf(wasCheckedin)
    override fun toString(): String =
        "Freetraining[id=$id, name=$name, venue.slug=${venue.slug}, date=$date, isScheduled=$isScheduled, wasCheckedin=$wasCheckedin]"
}
