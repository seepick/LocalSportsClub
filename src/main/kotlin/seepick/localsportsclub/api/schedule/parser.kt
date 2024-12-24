package seepick.localsportsclub.api.schedule

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

data class ScheduleInfo(
    val activityIds: List<Int>
)

object ScheduleParser {
    private val log = logger {}
    fun parse(html: String): ScheduleInfo {
        val document = Jsoup.parse(html)
        val htmlTag = document.childNodes().single { it.nodeName() == "html" }
        val body = htmlTag.childNodes().single { it.nodeName() == "body" } as Element
        val divs = body.select("div.reservations div.timetable div[class=\"smm-class-snippet row\"]").toList()
        return ScheduleInfo(divs.map { div ->
            div.attr("data-appointment-id").toInt()
        }).also {
            log.debug { "Parsed ${divs.size} reservation <div> tags to: $it" }
        }
    }
}
