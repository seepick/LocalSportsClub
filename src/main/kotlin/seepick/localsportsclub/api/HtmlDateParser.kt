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
    fun parseTime(text: String): TimePairs = text.split("—").map { twoTimes ->
        twoTimes.trim().split(":").let { numberParts ->
            require(numberParts.size == 2) { "Expected to be 2 number parts: ${numberParts.size} ($text)" }
            numberParts[0].toInt() to numberParts[1].toInt()
        }
    }.let { twoTimesList ->
        require(twoTimesList.size == 2) { "Times list expected to be 2 but was: ${twoTimesList.size} ($text)" }
        TimePairs(twoTimesList[0].toLocalTime(), twoTimesList[1].toLocalTime())
    }

    private val dayMonthFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM", Locale.ENGLISH)

    /**
     * @param dateString e.g. "Friday, 27 December | 10:00 —11:15"
     */
    fun parseDateTimeRange(dateString: String, year: Int): DateTimeRange {
        val (datePart, timePart) = dateString.split("|").also {
            require(it.size == 2) { "Invalid elements, expected size 2: $it" }
        }.let { it[0].trim() to it[1].trim() }
        val dateTemporal = dayMonthFormatter.parse(datePart)
        val date = LocalDate.of(year, dateTemporal[ChronoField.MONTH_OF_YEAR], dateTemporal[ChronoField.DAY_OF_MONTH])
        val times = parseTime(timePart)
        return DateTimeRange(
            start = LocalDateTime.of(date, times.from),
            end = LocalDateTime.of(date, times.to),
        )
    }
}

private fun Pair<Int, Int>.toLocalTime() = LocalTime.of(first, second)
