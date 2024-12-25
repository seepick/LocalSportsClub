package seepick.localsportsclub.api

import seepick.localsportsclub.service.DateTimeRange
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale

data class TimePairs(
    val from: LocalTime,
    val to: LocalTime,
)

object HtmlDateParser {

    private val dayMonthFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)

    /**
     * @param dateString e.g. "Friday, 27 December"
     */
    fun parseDate(dateString: String, year: Int): LocalDate {
        val dateTemporal = dayMonthFormatter.parse(dateString)
        return LocalDate.of(year, dateTemporal[ChronoField.MONTH_OF_YEAR], dateTemporal[ChronoField.DAY_OF_MONTH])
    }

    /**
     * @param timeString e.g. "13:14—15:16"
     */
    fun parseTime(timeString: String): TimePairs = timeString.split("—").map { twoTimes ->
        twoTimes.trim().split(":").let { numberParts ->
            require(numberParts.size == 2) { "Expected to be 2 number parts: ${numberParts.size} ($timeString)" }
            numberParts[0].toInt() to numberParts[1].toInt()
        }
    }.let { twoTimesList ->
        require(twoTimesList.size == 2) { "Times list expected to be 2 but was: ${twoTimesList.size} ($timeString)" }
        TimePairs(twoTimesList[0].toLocalTime(), twoTimesList[1].toLocalTime())
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
            start = LocalDateTime.of(date, times.from),
            end = LocalDateTime.of(date, times.to),
        )
    }
}

private fun Pair<Int, Int>.toLocalTime() = LocalTime.of(first, second)
