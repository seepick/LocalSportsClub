package seepick.localsportsclub.api.checkin

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import seepick.localsportsclub.api.HtmlDateParser
import java.time.LocalDate

data class CheckinsPage(
    val entries: List<CheckinEntry>,
) {
    companion object {
        val empty = CheckinsPage(emptyList())
    }

    val isEmpty = entries.isEmpty()
}

data class CheckinEntry(
    val date: LocalDate,
    val activityId: Int,
    val venueSlug: String,
)

object CheckinsParser {
    fun parse(rawHtml: String, currentYear: Int): CheckinsPage {
        val document = Jsoup.parse(rawHtml)
        val html = document.childNodes().single { it.nodeName() == "html" }
        val body = html.childNodes().single { it.nodeName() == "body" } as Element
        var currentDate: LocalDate? = null
        val entries = mutableListOf<CheckinEntry>()
        val timetable = body.select("div.timetable").first() ?: return CheckinsPage.empty
        timetable.children().forEach { sub ->
            when (sub.attr("class")) {
                "table-date" -> {
                    currentDate = HtmlDateParser.parseDate(sub.text().trim(), currentYear)
                }

                "smm-class-snippet row" -> {
                    entries += CheckinEntry(
                        date = currentDate!!,
                        activityId = sub.attr("data-appointment-id").toInt(),
                        venueSlug = sub.select("a.smm-studio-link").first()!!.attr("href").substringAfterLast("/"),
                    )
                }
            }
        }
        return CheckinsPage(entries)
    }
}
