package seepick.localsportsclub.service.date

import java.time.LocalTime

data class TimeRange(
    val start: LocalTime,
    val end: LocalTime,
) {
    init {
        require(start <= end) { "Start ($start) must be <= end ($end)" }
    }

    companion object {
        operator fun invoke(fromTo: String) = DateParser.parseTime(fromTo, "-")
    }
}
