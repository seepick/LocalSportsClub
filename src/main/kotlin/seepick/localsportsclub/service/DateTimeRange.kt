package seepick.localsportsclub.service

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DateTimeRange(
    val start: LocalDateTime,
    val end: LocalDateTime,
) {
    init {
        require(start < end)
    }

    fun prettyPrint(currentYear: Int): String =
        start.format(if (start.year != currentYear) dayDateYearAndTimeFormatter else dayDateAndTimeFormatter) + "-" +
                end.format(timeOnlyFormatter)
}

private val dayDateYearAndTimeFormatter = DateTimeFormatter.ofPattern("E dd.MM.yy HH:mm", Locale.ENGLISH)
private val dayDateAndTimeFormatter = DateTimeFormatter.ofPattern("E dd.MM. HH:mm", Locale.ENGLISH)
private val timeOnlyFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
