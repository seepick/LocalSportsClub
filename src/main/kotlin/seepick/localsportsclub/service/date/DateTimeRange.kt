package seepick.localsportsclub.service.date

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class DateTimeRange(
    val from: LocalDateTime,
    val to: LocalDateTime,
) {
    init {
        require(from <= to) { "From ($from) must be <= to ($to)" }
    }

    private val fromDate = from.toLocalDate()
    private val fromTime = from.toLocalTime()

    fun isStartMatching(date: LocalDate, matchFrom: LocalTime? = null, matchTo: LocalTime? = null): Boolean {
        val dateMatches = fromDate.isEqual(date)
        if (matchFrom != null && matchTo != null) return dateMatches && fromTime >= matchFrom && fromTime <= matchTo
        if (matchFrom != null) return dateMatches && fromTime >= matchFrom
        if (matchTo != null) return dateMatches && fromTime <= matchTo
        return dateMatches
    }
}
