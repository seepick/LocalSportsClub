package seepick.localsportsclub

import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.SystemClock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun date(day: Int) = LocalDate.of(2025, 1, day)

fun LocalDate.createDaysUntil(daysAhead: Int): List<LocalDate> =
    (1..daysAhead).map { plusDays(it - 1L) }

fun LocalDate.toLocalDateTime() = LocalDateTime.of(this, LocalTime.of(0, 0))

class StaticClock(
    private var now: LocalDateTime = SystemClock.now(),
    private var today: LocalDate = now.toLocalDate(),
) : Clock {

    fun setSeperately(time: LocalTime, day: LocalDate) {
        now = day.atTime(time)
        today = day
    }

    fun setNowAndToday(now: LocalDateTime) {
        this.now = now
        today = now.toLocalDate()
    }

    override fun now() = now
    override fun today() = today
}

