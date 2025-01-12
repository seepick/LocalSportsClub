package seepick.localsportsclub.sync.thirdparty

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse
import seepick.localsportsclub.service.date.DateTimeRange
import java.time.LocalDateTime

class MovementsYogaParserTest : StringSpec() {
    init {
        "When parse Then return" {
            val events = MovementsYogaParser.parse(readTestResponse("/thirdparty/movementsyoga.html"))
            events.size shouldBe 50
            events.first() shouldBe ThirdEvent(
                title = "HOT PILATES",
                teacher = "Roxann",
                dateTimeRange = DateTimeRange(
                    from = LocalDateTime.of(2025, 1, 19, 9, 30),
                    to = LocalDateTime.of(2025, 1, 19, 10, 30),
                ),
            )
        }
    }
}
