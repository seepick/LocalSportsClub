package seepick.localsportsclub.api.checkin

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import seepick.localsportsclub.service.date.DateParser
import seepick.localsportsclub.service.date.TimeRange
import java.time.LocalDate

data class CheckinsPage(
    val entries: List<CheckinEntry>,
) {
    companion object {
        val empty = CheckinsPage(emptyList())
    }

    val isEmpty = entries.isEmpty()
}

sealed interface CheckinEntry {
    val venueSlug: String
    val date: LocalDate
}

data class ActivityCheckinEntry(
    val activityId: Int,
    override val venueSlug: String,
    override val date: LocalDate,
    val timeRange: TimeRange,
    val isNoShow: Boolean,
) : CheckinEntry

data class FreetrainingCheckinEntry(
    val freetrainingId: Int,
    override val venueSlug: String,
    override val date: LocalDate,
) : CheckinEntry

object CheckinsParser {
    fun parse(rawHtml: String, today: LocalDate): CheckinsPage {
        val document = Jsoup.parse(rawHtml)
        val html = document.childNodes().single { it.nodeName() == "html" }
        val body = html.childNodes().single { it.nodeName() == "body" } as Element
        var currentDate: LocalDate? = null
        val entries = mutableListOf<CheckinEntry>()
        val timetable = body.select("div.timetable").first() ?: return CheckinsPage.empty
        timetable.children().forEach { sub ->
            when (sub.attr("class")) {
                "table-date" -> {
                    currentDate = DateParser.parseDate(sub.text().trim(), today.year).let {
                        // transitioning to next year
                        if (it <= today) it else it.withYear(today.year - 1)
                    }
                }

                "smm-class-snippet row" -> {
                    val id = sub.attr("data-appointment-id").toInt()
                    val time = sub.select("p.smm-class-snippet__class-time")
                    val venueSlug = sub.select("a.smm-studio-link").first()!!.attr("href").substringAfterLast("/")
                    entries += if (time.isEmpty()) { // it's a freetraining checkin as we got no time info for it
                        FreetrainingCheckinEntry(
                            date = currentDate!!,
                            freetrainingId = id,
                            venueSlug = venueSlug,
                        )
                    } else {
                        ActivityCheckinEntry(
                            date = currentDate!!,
                            activityId = id,
                            venueSlug = venueSlug,
                            timeRange = DateParser.parseTimes(sub.select("p.smm-class-snippet__class-time").text()),
                            isNoShow = sub.select("span.smm-booking-state-label").hasClass("noshow"),
                        )
                    }
                }
            }
        }
        return CheckinsPage(entries)
    }
}
