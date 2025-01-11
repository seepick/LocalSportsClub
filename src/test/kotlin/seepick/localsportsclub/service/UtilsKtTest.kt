package seepick.localsportsclub.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class UtilsKtTest : DescribeSpec() {
    init {
        describe("String.ensureMaxLength") {
            it("split") {
                "12345xyz".ensureMaxLength(5) shouldBe "12345\nxyz"
            }
            it("too short") {
                "123".ensureMaxLength(5) shouldBe "123"
            }
            it("multi line") {
                "123456\nxy".ensureMaxLength(5) shouldBe "12345\n6\nxy"
            }
        }
    }
}
