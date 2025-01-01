package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.view.shared.ScreenItem
import java.time.LocalDate

class Freetraining(
    val id: Int,
    override val venue: SimpleVenue,
    val name: String,
    val category: String,
    val date: LocalDate,
    wasCheckedin: Boolean,
) : ScreenItem {
    var wasCheckedin: Boolean by mutableStateOf(wasCheckedin)
    override fun toString(): String =
        "Freetraining[id=$id, name=$name, venue.slug=${venue.slug}, date=$date, wasCheckedin=$wasCheckedin]"
}
