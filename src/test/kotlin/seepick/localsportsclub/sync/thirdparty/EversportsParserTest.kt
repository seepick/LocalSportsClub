package seepick.localsportsclub.sync.thirdparty

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse
import seepick.localsportsclub.service.date.DateTimeRange
import java.time.LocalDateTime

class EversportsParserTest : StringSpec() {
    init {
        "When parse hotflowyoga html Then return" {
            val events = EversportsParser.parse(readTestResponse("/thirdparty/eversports.hotflowyoga.html"))

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
        "When parse movementamsterdam html Then return" {
            val events = EversportsParser.parse(readTestResponse("/thirdparty/eversports.movementamsterdam.html"))

            events.shouldHaveSize(19)
            val from = LocalDateTime.of(2025, 1, 13, 8, 30)
            events.first() shouldBe ThirdEvent(
                title = "Movement",
                teacher = "Nelson",
                dateTimeRange = DateTimeRange(
                    from = from,
                    to = from.plusMinutes(60),
                ),
            )
        }
        "When parse movementamsterdam html 2 Then return" {
            val events = EversportsParser.parse(readTestResponse("/thirdparty/eversports.movementamsterdam2.html"))

            val teachers = events.map { it.teacher }.distinct().toSet()
            teachers shouldBe setOf("Chris", "Nelson", "Jim", "Stas", "Mayke", "Farid")
        }
    }
}
