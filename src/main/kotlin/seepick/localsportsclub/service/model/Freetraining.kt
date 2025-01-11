package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.view.LscIcons
import seepick.localsportsclub.view.shared.HasVenue
import java.time.LocalDate

enum class FreetrainingState {
    // CAVE: names are used for DB mapping!
    Blank, Scheduled, Checkedin;

    fun iconStringAndSuffix() = when (this) {
        Blank -> ""
        Scheduled -> "${LscIcons.booked} "
        Checkedin -> "${LscIcons.checkedin} "
    }
}

class Freetraining(
    val id: Int,
    override val venue: Venue,
    val name: String,
    val category: String,
    val date: LocalDate,
    state: FreetrainingState
) : HasVenue {
    var state: FreetrainingState by mutableStateOf(state)
    val isScheduled get() = state == FreetrainingState.Scheduled
    val isCheckedin get() = state == FreetrainingState.Checkedin

    override fun toString(): String =
        "Freetraining[id=$id, name=$name, venue.slug=${venue.slug}, date=$date, state=$state]"
}
