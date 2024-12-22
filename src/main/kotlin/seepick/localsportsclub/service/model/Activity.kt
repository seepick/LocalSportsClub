package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class Activity(
    val id: Int,
    val venue: SimpleVenue,
    val name: String,
    val category: String, // aka disciplines/facilities
    val from: LocalDateTime,
    val to: LocalDateTime,
    spotsLeft: Int,
) {
    var spotsLeft: Int by mutableStateOf(spotsLeft)
    val fromToFormatted = buildDateTimeFormatted(from, to)

    override fun toString() = "Activity[id=$id, name=$name, venue.slug=${venue.slug}]"
}

private fun buildDateTimeFormatted(from: LocalDateTime, to: LocalDateTime): String =
    from.format(if (from.year != LocalDate.now().year) dayDateYearAndTimeFormatter else dayDateAndTimeFormatter) + "-" +
            to.format(timeOnlyFormatter)

private val dayDateYearAndTimeFormatter = DateTimeFormatter.ofPattern("E dd.MM.yy HH:mm", Locale.ENGLISH)
private val dayDateAndTimeFormatter = DateTimeFormatter.ofPattern("E dd.MM. HH:mm", Locale.ENGLISH)
private val timeOnlyFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
