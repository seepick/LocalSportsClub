package seepick.localsportsclub.service.date

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class DateTimeRange(
    val start: LocalDateTime,
    val end: LocalDateTime,
) {
    init {
        require(start <= end) { "Start ($start) must be <= end ($end)" }
    }

    private val startDate = start.toLocalDate()
    private val startTime = start.toLocalTime()

    fun isStartMatching(date: LocalDate, timeFrom: LocalTime? = null, timeTo: LocalTime? = null): Boolean {
        val dateMatches = startDate.isEqual(date)
        if (timeFrom != null && timeTo != null) return dateMatches &&
                startTime.isAfterOrEqual(timeFrom) &&
                startTime.isBeforeOrEqual(timeTo)
        if (timeFrom != null) return dateMatches && startTime.isAfterOrEqual(timeFrom)
        if (timeTo != null) return dateMatches && startTime.isBeforeOrEqual(timeTo)
        return dateMatches
    }
}
