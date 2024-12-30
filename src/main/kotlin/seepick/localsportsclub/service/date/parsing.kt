package seepick.localsportsclub.service.date


import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.util.Locale

object DateParser {

    private val dayMonthFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)
    private val timeParser = DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH)

    /**
     * @param dateString e.g. "Friday, 27 December"
     */
    fun parseDate(dateString: String, year: Int): LocalDate {
        val dateTemporal = dayMonthFormatter.parse(dateString)
        return LocalDate.of(year, dateTemporal[ChronoField.MONTH_OF_YEAR], dateTemporal[ChronoField.DAY_OF_MONTH])
    }

    /**
     * By default using the HTML &mdash; as a separator.
     * @param timeString e.g. "13:14—15:16"
     */
    fun parseTime(timeString: String, timeSeparator: String = "—"): TimeRange =
        timeString.split(timeSeparator).map { twoTimes ->
            twoTimes.trim().split(":").let { numberParts ->
                require(numberParts.size == 2) { "Expected to be 2 number parts but were ${numberParts.size} for time string: [$timeString]" }
                numberParts[0].toInt() to numberParts[1].toInt()
            }
        }.let { twoTimesList ->
            require(twoTimesList.size == 2) { "Times list expected to be 2 but was ${twoTimesList.size} for time string: [$timeString]" }
            TimeRange(twoTimesList[0].toLocalTime(), twoTimesList[1].toLocalTime())
        }

    /**
     * @param dateString e.g. "Friday, 27 December | 10:00 —11:15"
     */
    fun parseDateTimeRange(dateString: String, year: Int): DateTimeRange {
        val (datePart, timePart) = dateString.split("|").also {
            require(it.size == 2) { "Invalid elements, expected size 2: $it" }
        }.let { it[0].trim() to it[1].trim() }
        val date = parseDate(datePart, year)
        val times = parseTime(timePart)
        return DateTimeRange(
            start = LocalDateTime.of(date, times.start),
            end = LocalDateTime.of(date, times.end),
        )
    }

    fun parseTimeOrNull(string: String): LocalTime? =
        try {
            LocalTime.from(timeParser.parse(string))
        } catch (e: DateTimeParseException) {
            null
        }

}

private fun Pair<Int, Int>.toLocalTime() = LocalTime.of(first, second)
