package seepick.localsportsclub.service.date

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * @return "Sat 3.11."
 */
fun LocalDate.prettyPrint(currentYear: Int): String = if (year != currentYear) dayDateFormatterWithYear.format(this)
else dayDateFormatter.format(this)

private val machineDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
fun LocalDate.machinePrint(): String = machineDateFormatter.format(this)

/**
 * @return "3.11."
 */
fun LocalDate.prettyShortPrint(currentYear: Int): String = if (year != currentYear) dateFormatterWithYear.format(this)
else dateFormatter.format(this)

fun LocalDate.prettyPrintWith(time: LocalTime, currentYear: Int): String {
    val datetime = LocalDateTime.of(this, time)
    return if (year != currentYear) dayDatetimeFormatterWithYear.format(datetime)
    else dayDatetimeFormatter.format(datetime)
}

fun LocalTime.prettyPrint(): String = timeFormatter.format(this)

/**
 * @return "Sat 3.11. 04:05-04:06"
 */
fun DateTimeRange.prettyPrint(currentYear: Int): String =
    from.format(if (from.year != currentYear) dayDatetimeFormatterWithYear else dayDatetimeFormatter) + "-" + to.format(
        timeFormatter
    )

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

private val dateFormatter = DateTimeFormatter.ofPattern("d.M", Locale.ENGLISH)
private val dateFormatterWithYear = DateTimeFormatter.ofPattern("d.M.yy", Locale.ENGLISH)

private val dayDateFormatter = DateTimeFormatter.ofPattern("E d.M", Locale.ENGLISH)
private val dayDateFormatterWithYear = DateTimeFormatter.ofPattern("E d.M.yy", Locale.ENGLISH)

private val dayDatetimeFormatterWithYear = DateTimeFormatter.ofPattern("E d.M.yy HH:mm", Locale.ENGLISH)
private val dayDatetimeFormatter = DateTimeFormatter.ofPattern("E d.M. HH:mm", Locale.ENGLISH)
