package seepick.localsportsclub.api

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.service.DateTimeRange
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

            }
        }
        describe("parseDateTimeRange") {
            it("successfully") {
                HtmlDateParser.parseDateTimeRange(
                    "Friday, 27 December | 10:00 —11:15", 2024
                ) shouldBe DateTimeRange(
                    start = LocalDateTime.of(2024, 12, 27, 10, 0),
                    end = LocalDateTime.of(2024, 12, 27, 11, 15)
                )
            }
        }
    }
}
