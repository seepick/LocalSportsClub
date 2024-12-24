package seepick.localsportsclub.api.schedule

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

data class ScheduleHtml(
    val rows: List<ScheduleRow>
)

data class ScheduleRow(
    val activityId: Int,
    val venueSlug: String,
)

object ScheduleParser {

    private val log = logger {}

    fun parse(html: String): ScheduleHtml {
        val document = Jsoup.parse(html)
        val htmlTag = document.childNodes().single { it.nodeName() == "html" }
        val body = htmlTag.childNodes().single { it.nodeName() == "body" } as Element
        val divs = body.select("div.reservations div.timetable div[class=\"smm-class-snippet row\"]").toList()


        return ScheduleHtml(rows = divs.map { div ->
            ScheduleRow(
                activityId = div.attr("data-appointment-id").toInt(),
                venueSlug = div.select("a.smm-studio-link").attr("href").substringAfterLast("/"),
            )
        }).also {
            log.debug { "Parsed ${divs.size} reservation <div> tags to: $it" }
        }
    }
}
