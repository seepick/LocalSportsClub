package seepick.localsportsclub

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FailingTest : StringSpec() {
    init {
        "nope" {
            // yep, fail
            1 shouldBe 2
        }
    }
}
