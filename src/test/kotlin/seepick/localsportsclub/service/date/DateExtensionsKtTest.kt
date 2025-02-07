package seepick.localsportsclub.service.date

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class DateExtensionsKtTest : DescribeSpec() {
    init {
        describe("atEndOfMonth") {
            it("tests") {
                LocalDate.of(2025, 1, 15).atEndOfMonth() shouldBe LocalDate.of(2025, 1, 31)
                LocalDate.of(2025, 2, 15).atEndOfMonth() shouldBe LocalDate.of(2025, 2, 28)
                LocalDate.of(2025, 4, 15).atEndOfMonth() shouldBe LocalDate.of(2025, 4, 30)
                LocalDate.of(2025, 12, 15).atEndOfMonth() shouldBe LocalDate.of(2025, 12, 31)
            }
        }
    }
}
