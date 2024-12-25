package seepick.localsportsclub.api.checkin

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse
import java.time.LocalDate

class CheckinsParserTest : StringSpec() {
    private val year = 2024
    private val anyYear = 1234

    private fun parseTestFile(fileName: String, year: Int = anyYear) =
        CheckinsParser.parse(readTestResponse<String>(fileName), year)

    init {
        "When parse some Then return them" {
            val result = parseTestFile("checkins.html", year)

            result.entries shouldBe listOf(
                CheckinEntry(LocalDate.of(year, 12, 24), 84726253, "yoga-spot-olympisch-stadion"),
                CheckinEntry(LocalDate.of(year, 12, 24), 84742854, "studio-108-3"),
                CheckinEntry(LocalDate.of(year, 12, 23), 83535971, "de-nieuwe-yogaschool"),
            )
        }
        "When parse empty Then return empty" {
            val result = parseTestFile("checkins.empty.html")

            result.entries.shouldBeEmpty()
        }
    }
}
