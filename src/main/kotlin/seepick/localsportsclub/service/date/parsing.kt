package seepick.localsportsclub.service.date


import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object DateParser {

    private val timeParser = DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH)

//    /**
//     * By default using the HTML &mdash; as a separator.
//     * @param timeString e.g. "13:14—15:16"
//     */
//    fun parseTimes(timeString: String, timeSeparator: String = "—"): TimeRange =
//        timeString.split(timeSeparator).map { twoTimes ->
//            twoTimes.trim().split(":").let { numberParts ->
//                require(numberParts.size == 2) { "Expected to be 2 number parts but were ${numberParts.size} for time string: [$timeString]" }
//                numberParts[0].toInt() to numberParts[1].toInt()
//            }
//        }.let { twoTimesList ->
//            require(twoTimesList.size == 2) { "Times list expected to be 2 but was ${twoTimesList.size} for time string: [$timeString]" }
//            TimeRange(twoTimesList[0].toLocalTime(), twoTimesList[1].toLocalTime())
//        }

    /** @param time e.g. "3:15" or "18:58" */
    fun parseTimeOrNull(time: String): LocalTime? =
        try {
            LocalTime.from(timeParser.parse(time))
        } catch (_: DateTimeParseException) {
            null
        }
}

private fun Pair<Int, Int>.toLocalTime() = LocalTime.of(first, second)
