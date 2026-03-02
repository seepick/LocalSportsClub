package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.common.table.TableItemBgColor
import java.time.LocalDate

enum class FreetrainingState {
    // CAVE: names are used for DB mapping!
    Blank, Scheduled, Checkedin;

    fun iconStringAndSuffix() = when (this) {
        Blank -> ""
        Scheduled -> "${LscIcons.reservedEmoji} "
        Checkedin -> "${LscIcons.checkedinEmoji} "
    }
}

class Freetraining(
    val id: Int,
    override val venue: Venue,
    val name: String,
    val category: Category,
    val date: LocalDate,
    state: FreetrainingState,
) : HasVenue, TableItemBgColor by venue, HasDistance by venue {
    var state: FreetrainingState by mutableStateOf(state)

    fun isInPast(today: LocalDate): Boolean = date < today

    override fun toString(): String =
        "Freetraining[id=$id, name=$name, venue.slug=${venue.slug}, date=$date, state=$state]"

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Freetraining) return false

        return id == other.id && name == other.name && date == other.date && category == other.category && venue.id == other.venue.id
    }

    override fun hashCode() =
        id.hashCode() * 31 + name.hashCode() * 31 + date.hashCode() * 31 + category.hashCode() * 31 + venue.id.hashCode()

    companion object {
        fun comparator(today: LocalDate) = Comparator<Freetraining> { f1, f2 ->
            val f1IsNew = f1.date >= today
            val f2IsNew = f2.date >= today
            if (f1IsNew && f2IsNew) {
                f1.date.compareTo(f2.date)
            } else if (!f1IsNew && !f2IsNew) {
                f2.date.compareTo(f1.date)
            } else {
                if (f1IsNew) -1 else 1
            }
        }
    }
}
