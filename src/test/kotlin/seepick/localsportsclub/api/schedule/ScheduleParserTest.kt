package seepick.localsportsclub.api.schedule

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse

class ScheduleParserTest : StringSpec() {
    init {
        "When parse Then return entries" {
            val schedule = ScheduleParser.parse(readTestResponse("schedule.html"))

            schedule shouldBe ScheduleHtml(
                rows = listOf(
                    ScheduleRow(84742854, "studio-108-3"),
                    ScheduleRow(84726253, "yoga-spot-olympisch-stadion"),
                    ScheduleRow(84810748, "movements-city"),
                )
            )
        }
    }
}
