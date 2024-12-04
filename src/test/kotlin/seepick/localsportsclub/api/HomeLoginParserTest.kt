package seepick.localsportsclub.api

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.junit.Test
import seepick.localsportsclub.readFromClasspath

class HomeLoginParserTest : StringSpec() {
    init {
        "When parse home response Then extract login secret" {
            val result = HomeLoginParser.parse(readFromClasspath("/test_lsc/response_home.html"))

            result.loginSecret shouldBe ("UWZZNDJwNmEvaS9YTHZHN01XQ2QxQT09" to "SlVBNHExWEQ4bncyQTZiRnBrcVNYQT09")
        }
    }
}
