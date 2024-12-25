package seepick.localsportsclub.api.activity

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import seepick.localsportsclub.api.HtmlDateParser
import seepick.localsportsclub.api.TimePairs
import seepick.localsportsclub.kotlinxSerializer
import java.time.LocalDate
import java.time.LocalDateTime


data class ActivityInfo(
    val id: Int,
    val name: String,
    val venueSlug: String,
    val from: LocalDateTime,
    val to: LocalDateTime,
    val category: String, // aka disciplines/facilities
    val spotsLeft: Int,
    // membership plan support ...
    // type = "instant booking"
)

@Serializable
data class ActivityDataLayerJson(
    // event: String,
    // user: ...,
    val `class`: ActivityDataLayerClassJson,
)

@Serializable
data class ActivityDataLayerClassJson(
    val id: String,
    val name: String,
    val category: String,
    val spots_left: String,
)

object ActivitiesParser {
    private val log = logger {}

    fun parse(htmlString: String, date: LocalDate): List<ActivityInfo> {
        val document = Jsoup.parse(htmlString)
        val html = document.childNodes()[0] as Element
        val body = html.children()[1]
        val divs = body.children()
        log.debug { "Parsing ${divs.size} activities." }
        return divs.map { div ->
            parseSingle(div, date)
        }
    }

    private fun parseSingle(div: Element, date: LocalDate): ActivityInfo {
        val dataLayerJsonString = div.select("a[href=\"#modal-class\"]").first()!!.attr("data-datalayer")
        val dataLayer = kotlinxSerializer.decodeFromString<ActivityDataLayerJson>(dataLayerJsonString).`class`
        val (from, to) = convertFromToDateTime(
            date, HtmlDateParser.parseTime(div.select("p.smm-class-snippet__class-time").text())
        )
        require(
            div.attr("data-appointment-id").toInt() == dataLayer.id.toInt()
        ) { "IDs expected to be identical but weren't!" }
        return ActivityInfo(
            id = dataLayer.id.toInt(),
            name = dataLayer.name,
            venueSlug = div.select("a.smm-studio-link").first()!!.attr("href").substringAfterLast("/"),
            from = from,
            to = to,
            category = dataLayer.category,
            spotsLeft = dataLayer.spots_left.toInt(),
        )
    }
}

private fun convertFromToDateTime(date: LocalDate, times: TimePairs): Pair<LocalDateTime, LocalDateTime> =
    LocalDateTime.of(date, times.from) to LocalDateTime.of(date, times.to)

@Serializable
data class ActivityBookDataJson(
    val `class`: ActivityDataLayerClassJson,
    val venue: ActivityDataLayerVenueJson,
)

@Serializable
data class ActivityCancelDataJson(
    val `class`: ActivityDataLayerCancelClassJson,
    val venue: ActivityDataLayerVenueJson,
)

@Serializable
data class ActivityDataLayerCancelClassJson(
    val id: String,
    val name: String,
    val category: String,
)

@Serializable
data class ActivityDataLayerVenueJson(
    val id: Int, // that's the USC internal ID, don't use it (not stable anyway)
    val name: String,
)

object ActivityParser {
    fun parse(html: String, currentYear: Int): ActivityDetail {
        val document = Jsoup.parse(html)
        val root = document.childNodes()[0] as Element
        val body = root.children()[1]
        val div = body.children().first()!!

        val dateString = div.select("p.smm-class-details__datetime").text()
        val dateRange = HtmlDateParser.parseDateTimeRange(dateString, currentYear)
        val buttonBook = div.select("button.book")
        return if (buttonBook.hasAttr("data-book-success")) {
            val json = buttonBook.attr("data-book-success")
            val data = kotlinxSerializer.decodeFromString<ActivityBookDataJson>(json)
            ActivityDetail(
                name = data.`class`.name.trim(),
                dateTimeRange = dateRange,
                venueName = data.venue.name.trim(),
                category = data.`class`.category.trim(),
                spotsLeft = data.`class`.spots_left.toInt(),
            )
        } else {
            val buttonCancel = div.select("button.cancel")
            val json = buttonCancel.attr("data-book-cancel")
            val data = kotlinxSerializer.decodeFromString<ActivityCancelDataJson>(json)
            ActivityDetail(
                name = data.`class`.name.trim(),
                dateTimeRange = dateRange,
                venueName = data.venue.name.trim(),
                category = data.`class`.category.trim(),
                spotsLeft = 0,
            )
        }
    }
}
