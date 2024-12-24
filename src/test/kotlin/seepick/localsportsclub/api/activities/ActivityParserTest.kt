package seepick.localsportsclub.api.activities

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse
import seepick.localsportsclub.service.DateTimeRange
import java.time.LocalDateTime

class ActivityParserTest : StringSpec() {
    init {
        "When parse Then return" {
            val year = 2024
            ActivityParser.parse(readTestResponse("activity_detail.html"), year) shouldBe ActivityDetail(
                name = "RESTORATIVE YOGA",
                dateTimeRange = DateTimeRange(
                    start = LocalDateTime.of(year, 12, 27, 10, 0),
                    end = LocalDateTime.of(year, 12, 27, 11, 15)
                ),
                venueId = 25336,
                venueName = "Movements City",
                category = "Yoga",
                spotsLeft = 2,
            )
        }
    }
}
