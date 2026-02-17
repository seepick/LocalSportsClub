package seepick.localsportsclub.sync.thirdparty

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse
import seepick.localsportsclub.service.date.DateTimeRange
import java.time.LocalDateTime

class MovementsYogaParserTest : StringSpec() {
    init {
        fun parseResponse(fileName: String) = MovementsYogaParser.parse(readTestResponse("thirdparty/$fileName"))

        "When parse Then return" {
            val events = parseResponse("movementsyoga.html")
            events.size shouldBe 49
            events.map { it.title } shouldNotContain "HOT CANCELLED"
            events.first() shouldBe ThirdEvent(
                title = "HOT PILATES",
                teacher = "Roxann",
                dateTimeRange = DateTimeRange(
                    from = LocalDateTime.of(2025, 1, 19, 9, 30),
                    to = LocalDateTime.of(2025, 1, 19, 10, 30),
                ),
            )
        }
        "When parse empty time string Then be able to parse" {
            parseResponse("movementsyoga-empty_day.html")
        }
        "When parse empty duration string Then be able to parse" {
            parseResponse("movementsyoga-empty_duration.html")
        }
        "When parse empty duration string for another reason Then be able to parse" {
            parseResponse("movementsyoga-empty_duration2.html")
        }
    }
}
