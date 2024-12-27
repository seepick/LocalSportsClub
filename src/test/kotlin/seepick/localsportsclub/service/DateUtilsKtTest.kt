package seepick.localsportsclub.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DateUtilsKtTest : DescribeSpec() {
    init {
        describe("LocalDate.prettyPrint") {
            it("simple") {
                LocalDate.of(2001, 12, 31).prettyPrint(2001) shouldBe "Mon 31.12"
            }
            it("past") {
                LocalDate.of(2001, 12, 31).prettyPrint(2002) shouldBe "Mon 31.12.01"
            }
        }
        describe("LocalDate.prettyPrintWith") {
            it("simple") {
                val time = LocalTime.of(12, 23, 59)
                LocalDate.of(2001, 12, 31).prettyPrintWith(time, 2001) shouldBe "Mon 31.12. 12:23"
            }
            it("past") {
                val time = LocalTime.of(12, 23, 59)
                LocalDate.of(2001, 12, 31).prettyPrintWith(time, 2002) shouldBe "Mon 31.12.01 12:23"
            }
        }
        describe("DateTimeRange.prettyPrint") {
            it("simple") {
                DateTimeRange(
                    start = LocalDateTime.of(2001, 11, 3, 4, 5, 6),
                    end = LocalDateTime.of(2001, 11, 3, 4, 6, 7),
                ).prettyPrint(2001) shouldBe "Sat 3.11. 04:05-04:06"
            }
            it("past") {
                DateTimeRange(
                    start = LocalDateTime.of(2001, 11, 3, 4, 5, 6),
                    end = LocalDateTime.of(2001, 11, 3, 4, 6, 7),
                ).prettyPrint(2002) shouldBe "Sat 3.11.01 04:05-04:06"
            }
        }
    }
}
