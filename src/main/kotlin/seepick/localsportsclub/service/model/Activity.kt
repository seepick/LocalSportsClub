package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    scheduled: Boolean,
) {
    var spotsLeft: Int by mutableStateOf(spotsLeft)
    var scheduled: Boolean by mutableStateOf(scheduled)
    fun fromToFormatted(currentYear: Int) = buildDateTimeFormatted(from, to, currentYear)

    override fun toString() = "Activity[id=$id, name=$name, venue.slug=${venue.slug}]"
}

private fun buildDateTimeFormatted(from: LocalDateTime, to: LocalDateTime, currentYear: Int): String =
    from.format(if (from.year != currentYear) dayDateYearAndTimeFormatter else dayDateAndTimeFormatter) + "-" +
            to.format(timeOnlyFormatter)

private val dayDateYearAndTimeFormatter = DateTimeFormatter.ofPattern("E dd.MM.yy HH:mm", Locale.ENGLISH)
private val dayDateAndTimeFormatter = DateTimeFormatter.ofPattern("E dd.MM. HH:mm", Locale.ENGLISH)
private val timeOnlyFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
