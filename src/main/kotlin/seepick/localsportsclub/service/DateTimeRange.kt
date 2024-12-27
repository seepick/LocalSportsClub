package seepick.localsportsclub.service

import java.time.LocalDateTime

data class DateTimeRange(
    val start: LocalDateTime,
    val end: LocalDateTime,
) {
    init {
        require(start < end)
    }
}
