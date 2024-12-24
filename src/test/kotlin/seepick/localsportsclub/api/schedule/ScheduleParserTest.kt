package seepick.localsportsclub.api.schedule

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse

class ScheduleParserTest : StringSpec() {
    init {
        "When parse Then return activity IDs" {
            val schedule = ScheduleParser.parse(readTestResponse("schedule.html"))

            schedule shouldBe ScheduleInfo(activityIds = listOf(84742854, 84726253, 84810748))
        }
    }
}
