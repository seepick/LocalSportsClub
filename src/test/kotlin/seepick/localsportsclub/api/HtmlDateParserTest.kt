package seepick.localsportsclub.api

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.service.DateTimeRange
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class HtmlDateParserTest : DescribeSpec() {
    init {
        describe("parseTime") {
            it("successfully") {
                HtmlDateParser.parseTime("13:14—15:16") shouldBe TimePairs(
                    from = LocalTime.of(13, 14),
                    to = LocalTime.of(15, 16),
                )
                HtmlDateParser.parseTime("03:04—05:06") shouldBe TimePairs(
                    from = LocalTime.of(3, 4),
                    to = LocalTime.of(5, 6),
                )
            }
        }
        describe("parseDate") {
            it("successfully") {
                HtmlDateParser.parseDate("Monday, 9 December", 2024) shouldBe
                        LocalDate.of(2024, 12, 9)
                HtmlDateParser.parseDate("Tuesday, 24 December", 2024) shouldBe
                        LocalDate.of(2024, 12, 24)
            }
        }
        describe("parseDateTimeRange") {
            it("successfully") {
                HtmlDateParser.parseDateTimeRange(
                    "Monday, 9 December | 03:04 —05:06", 2024
                ) shouldBe DateTimeRange(
                    start = LocalDateTime.of(2024, 12, 9, 3, 4),
                    end = LocalDateTime.of(2024, 12, 9, 5, 6),
                )
                HtmlDateParser.parseDateTimeRange(
                    "Friday, 27 December | 10:00 —11:15", 2024
                ) shouldBe DateTimeRange(
                    start = LocalDateTime.of(2024, 12, 27, 10, 0),
                    end = LocalDateTime.of(2024, 12, 27, 11, 15),
                )
            }
        }
    }
}
