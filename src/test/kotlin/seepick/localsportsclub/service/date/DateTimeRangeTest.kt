package seepick.localsportsclub.service.date

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateTimeRangeTest : StringSpec() {
    private val anyTime = LocalTime.now()
    private val anyDateTime = LocalDateTime.now()

    init {
        "isStartMatching for date only" {
            val startDate = LocalDate.of(2024, 12, 5)
            val range = DateTimeRange(
                start = LocalDateTime.of(startDate, anyTime),
                end = anyDateTime,
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
            start = start,
            end = end,
        )
        "isStartMatching for date and time - timeFrom and timeTo" {
            range.isStartMatching(
                startDate,
                timeFrom = startTime.minusHours(1),
                timeTo = endTime.plusHours(1)
            ) shouldBe true

        }
        "isStartMatching for date and time - timeFrom" {
            range.isStartMatching(startDate, timeFrom = start.toLocalTime()) shouldBe true
            range.isStartMatching(startDate.plusDays(1), timeFrom = startTime) shouldBe false
            range.isStartMatching(startDate.minusDays(1), timeFrom = startTime) shouldBe false
            range.isStartMatching(startDate, timeFrom = startTime.minusMinutes(1)) shouldBe true
            range.isStartMatching(startDate, timeFrom = startTime.plusMinutes(1)) shouldBe false
            range.isStartMatching(startDate, timeFrom = startTime, timeTo = endTime) shouldBe true
        }
        "isStartMatching for date and time - timeTo" {
            range.isStartMatching(startDate, timeTo = startTime) shouldBe true
            range.isStartMatching(startDate, timeTo = startTime.plusMinutes(1)) shouldBe true
            range.isStartMatching(startDate, timeTo = startTime.minusMinutes(1)) shouldBe false
        }
    }
}
