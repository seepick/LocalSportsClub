package seepick.localsportsclub.service.date

import com.github.seepick.uscclient.utils.DateTimeRange
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateTimeRangeTest : StringSpec() {
    private val anyTime = LocalTime.now()
    private val anyDateTime = SystemClock.now()

    init {
        "isStartMatching for date only" {
            val startDate = LocalDate.of(2024, 12, 5)
            val range = DateTimeRange(
                from = LocalDateTime.of(startDate, anyTime),
                to = anyDateTime,
            )
            range.isStartMatching(startDate.minusDays(1)) shouldBe false
            range.isStartMatching(startDate) shouldBe true
            range.isStartMatching(startDate.plusDays(1)) shouldBe false
        }

        val start = LocalDateTime.of(2024, 12, 5, 14, 30)
        val startDate = start.toLocalDate()
        val startTime = start.toLocalTime()
        val end = LocalDateTime.of(2024, 12, 5, 15, 30)
        val endTime = end.toLocalTime()
        val range = DateTimeRange(
            from = start,
            to = end,
        )
        "isStartMatching for date and time - timeFrom and timeTo" {
            range.isStartMatching(
                startDate,
                matchFrom = startTime.minusHours(1),
                matchTo = endTime.plusHours(1)
            ) shouldBe true

        }
        "isStartMatching for date and time - timeFrom" {
            range.isStartMatching(startDate, matchFrom = start.toLocalTime()) shouldBe true
            range.isStartMatching(startDate.plusDays(1), matchFrom = startTime) shouldBe false
            range.isStartMatching(startDate.minusDays(1), matchFrom = startTime) shouldBe false
            range.isStartMatching(startDate, matchFrom = startTime.minusMinutes(1)) shouldBe true
            range.isStartMatching(startDate, matchFrom = startTime.plusMinutes(1)) shouldBe false
            range.isStartMatching(startDate, matchFrom = startTime, matchTo = endTime) shouldBe true
        }
        "isStartMatching for date and time - timeTo" {
            range.isStartMatching(startDate, matchTo = startTime) shouldBe true
            range.isStartMatching(startDate, matchTo = startTime.plusMinutes(1)) shouldBe true
            range.isStartMatching(startDate, matchTo = startTime.minusMinutes(1)) shouldBe false
        }
    }
}
