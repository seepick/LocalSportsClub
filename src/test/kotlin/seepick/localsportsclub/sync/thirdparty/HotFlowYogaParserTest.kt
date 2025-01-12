package seepick.localsportsclub.sync.thirdparty

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse
import seepick.localsportsclub.service.date.DateTimeRange
import java.time.LocalDateTime

class HotFlowYogaParserTest : StringSpec() {
    init {
        "When parse html Then return" {
            val events = HotFlowYogaParser.parse(readTestResponse("/thirdparty/hotflowyoga.html"))

            events.shouldHaveSize(74)
            val from = LocalDateTime.of(2025, 1, 12, 9, 0)
            events.first() shouldBe ThirdEvent(
                title = "Hot Vinyasa Flow",
                teacher = "David Rodrigues Junior",
                dateTimeRange = DateTimeRange(
                    from = from,
                    to = from.plusMinutes(60),
                ),
            )
        }
    }
}
