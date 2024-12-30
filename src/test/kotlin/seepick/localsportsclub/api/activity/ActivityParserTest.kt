package seepick.localsportsclub.api.activity

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse
import seepick.localsportsclub.service.date.DateTimeRange
import java.time.LocalDate
import java.time.LocalDateTime

class ActivityParserTest : StringSpec() {

    init {
        "When parse upcoming Then return" {
            ActivityParser.parse(readTestResponse("activity_detail.html"), 2024) shouldBe ActivityDetails(
                name = "RESTORATIVE YOGA",
                dateTimeRange = DateTimeRange(
                    start = LocalDateTime.of(2024, 12, 27, 10, 0),
                    end = LocalDateTime.of(2024, 12, 27, 11, 15)
                ),
                venueName = "Movements City",
                category = "Yoga",
                spotsLeft = 2,
            )
        }
        "When parse old Then return" {
            ActivityParser.parse(readTestResponse("activity_detail.past.html"), 2024) shouldBe ActivityDetails(
                name = "Sound Healing with Katty",
                dateTimeRange = DateTimeRange(
                    start = LocalDateTime.of(2024, 12, 24, 15, 0),
                    end = LocalDateTime.of(2024, 12, 24, 16, 0)
                ),
                venueName = "Yogaspot Olympisch Stadion",
                category = "Meditation",
                spotsLeft = 0, // for past, this is actually not available ;)
            )
        }
        "When parse single freetraining Then return" {
            ActivityParser.parseFreetraining(
                readTestResponse("activity_detail.freetraining.html"),
                2024
            ) shouldBe FreetrainingDetails(
                id = 83664090,
                name = "Wellness Spa",
                date = LocalDate.of(2024, 12, 29),
                venueSlug = "vitality-spa-fitness-amsterdam",
                category = "Wellness",
            )
        }
    }
}
