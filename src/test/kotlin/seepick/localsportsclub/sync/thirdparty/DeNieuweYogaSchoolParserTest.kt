package seepick.localsportsclub.sync.thirdparty

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse
import seepick.localsportsclub.service.date.DateTimeRange
import java.time.LocalDateTime

class DeNieuweYogaSchoolParserTest : StringSpec() {
    init {
        "When parse Then return" {
            val events = DeNieuweYogaSchoolParser.parse(readTestResponse("/thirdparty/denieuweyogaschool.html"))

            events.shouldHaveSize(8)
            val from = LocalDateTime.of(2025, 1, 16, 9, 0)
            events.first() shouldBe ThirdEvent(
                title = "Basic Flow (60 min) EN",
                teacher = "Liisa Russmann",
                dateTimeRange = DateTimeRange(
                    from = from,
                    to = from.plusMinutes(60),
                ),
            )
        }
    }
}
