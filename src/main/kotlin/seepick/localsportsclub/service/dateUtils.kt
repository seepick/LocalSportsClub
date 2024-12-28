package seepick.localsportsclub.service

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun LocalDate.prettyPrint(currentYear: Int): String = if (year != currentYear) dateFormatterWithYear.format(this)
else dateFormatter.format(this)


fun LocalDate.prettyPrintWith(time: LocalTime, currentYear: Int): String {
    val datetime = LocalDateTime.of(this, time)
    return if (year != currentYear) datetimeFormatterWithYear.format(datetime)
    else datetimeFormatter.format(datetime)
}

/**
 * @return "Sat 3.11. 04:05-04:06"
 */
fun DateTimeRange.prettyPrint(currentYear: Int): String =
    start.format(if (start.year != currentYear) datetimeFormatterWithYear else datetimeFormatter) + "-" + end.format(
        timeFormatter
    )

private val dateFormatter = DateTimeFormatter.ofPattern("E d.MM", Locale.ENGLISH)
private val dateFormatterWithYear = DateTimeFormatter.ofPattern("E d.MM.yy", Locale.ENGLISH)
private val datetimeFormatterWithYear = DateTimeFormatter.ofPattern("E d.MM.yy HH:mm", Locale.ENGLISH)
private val datetimeFormatter = DateTimeFormatter.ofPattern("E d.MM. HH:mm", Locale.ENGLISH)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
