package seepick.localsportsclub.api.activities

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ActivitiesParserTest : StringSpec() {
    private fun read(fileName: String, date: LocalDate): List<ActivityInfo> =
        ActivitiesParser.parse(readTestResponse<ActivitiesJson>(fileName).data.content, date)

    init {
        "fo ba" {
            val date = LocalDate.of(2024, 10, 22)
            read("activities.single.json", date).shouldBeSingleton().first() shouldBe ActivityInfo(
                id = 74626938,
                name = "Kickboks zaktraining",
                venueSlug = "basecampwest",
                category = "Mixed Martial Arts",
                spotsLeft = 7,
                from = LocalDateTime.of(date, LocalTime.of(7, 0)),
                to = LocalDateTime.of(date, LocalTime.of(7, 45)),
            )
        }
    }
}
