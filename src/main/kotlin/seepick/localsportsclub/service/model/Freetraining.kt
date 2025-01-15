package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.common.table.TableItemBgColor
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
) : HasVenue, TableItemBgColor by venue {
    var state: FreetrainingState by mutableStateOf(state)

    override fun toString(): String =
        "Freetraining[id=$id, name=$name, venue.slug=${venue.slug}, date=$date, state=$state]"
}
