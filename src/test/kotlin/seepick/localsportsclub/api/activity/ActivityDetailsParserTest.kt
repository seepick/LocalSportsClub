package seepick.localsportsclub.api.activity

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.service.model.Plan
import java.time.LocalDate
import java.time.LocalDateTime

class ActivityDetailsParserTest : StringSpec() {

    init {
        "Extract date time" {
            extractDateTime("If you need to cancel, please cancel your booking by 27/12/2024, 08:00.") shouldBe
                    LocalDateTime.of(2024, 12, 27, 8, 0)
        }
        "When parse upcoming Then return" {
            ActivityDetailsParser.parseDetails(readTestResponse("activity_detail.html"), 2024) shouldBe ActivityDetails(
                name = "RESTORATIVE YOGA",
                dateTimeRange = DateTimeRange(
                    from = LocalDateTime.of(2024, 12, 27, 10, 0),
                    to = LocalDateTime.of(2024, 12, 27, 11, 15)
                ),
                venueName = "Movements City",
                category = "Yoga",
                spotsLeft = 2,
                cancellationDateLimit = LocalDateTime.of(2024, 12, 27, 8, 0),
                plan = Plan.UscPlan.Medium,
            )
        }
        "When parse old Then return" {
            ActivityDetailsParser.parseDetails(
                readTestResponse("activity_detail.past.html"),
                2024
            ) shouldBe ActivityDetails(
                name = "Sound Healing with Katty",
                dateTimeRange = DateTimeRange(
                    from = LocalDateTime.of(2024, 12, 24, 15, 0),
                    to = LocalDateTime.of(2024, 12, 24, 16, 0)
                ),
                venueName = "Yogaspot Olympisch Stadion",
                category = "Meditation",
                spotsLeft = 0, // for past, this is actually not available ;)
                cancellationDateLimit = null,
                plan = Plan.UscPlan.Small,
            )
        }
        "When parse single freetraining Then return" {
            ActivityDetailsParser.parseFreetraining(
                readTestResponse("activity_detail.freetraining.html"),
                2024
            ) shouldBe FreetrainingDetails(
                id = 83664090,
                name = "Wellness Spa",
                date = LocalDate.of(2024, 12, 29),
                venueSlug = "vitality-spa-fitness-amsterdam",
                category = "Wellness",
                plan = Plan.UscPlan.Small,
            )
        }
    }
}
