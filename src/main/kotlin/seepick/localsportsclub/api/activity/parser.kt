package seepick.localsportsclub.api.activity

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import seepick.localsportsclub.kotlinxSerializer
import seepick.localsportsclub.service.date.DateParser
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.service.date.TimeRange
import java.time.LocalDate
import java.time.LocalDateTime

data class ActivityInfo(
    val id: Int,
    val name: String,
    val venueSlug: String,
    val dateTimeRange: DateTimeRange,
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

data class FreetrainingInfo(
    val id: Int,
    val name: String,
    val category: String,
    val venueSlug: String,
)

object ActivitiesParser {
    private val log = logger {}

    fun parseContent(htmlString: String, date: LocalDate): List<ActivityInfo> {
        val document = Jsoup.parse(htmlString)
        val html = document.childNodes()[0] as Element
        val body = html.children()[1]
        val divs = body.children()
        log.debug { "Parsing ${divs.size} activities." }
        return divs.map { div ->
            parseSingle(div, date)
        }
    }

    fun parseFreetrainingContent(htmlString: String): List<FreetrainingInfo> {
        val document = Jsoup.parse(htmlString)
        val html = document.childNodes()[0] as Element
        val body = html.children()[1]
        val divs = body.children()
        log.debug { "Parsing ${divs.size} freetrainings." }
        return divs.map { div ->
            FreetrainingInfo(
                id = div.attr("data-appointment-id").toInt(),
                name = div.select("div.title a.title").text().trim(),
                category = div.select("div.title p").text().trim(),
                venueSlug = div.select("a.smm-studio-link").first()!!.attr("href").substringAfterLast("/"),
            )
        }
    }

    private fun parseSingle(div: Element, date: LocalDate): ActivityInfo {
        val dataLayerJsonString = div.select("a[href=\"#modal-class\"]").first()!!.attr("data-datalayer")
        val dataLayer = kotlinxSerializer.decodeFromString<ActivityDataLayerJson>(dataLayerJsonString).`class`
        val dateTimeRange = convertFromToDateTime(
            date, DateParser.parseTime(div.select("p.smm-class-snippet__class-time").text())
        )
        require(
            div.attr("data-appointment-id").toInt() == dataLayer.id.toInt()
        ) { "IDs expected to be identical but weren't!" }
        return ActivityInfo(
            id = dataLayer.id.toInt(),
            name = dataLayer.name,
            venueSlug = div.select("a.smm-studio-link").first()!!.attr("href").substringAfterLast("/"),
            dateTimeRange = dateTimeRange,
            category = dataLayer.category,
            spotsLeft = dataLayer.spots_left.toInt(),
        )
    }
}

private fun convertFromToDateTime(date: LocalDate, times: TimeRange): DateTimeRange =
    DateTimeRange(
        from = LocalDateTime.of(date, times.start),
        to = LocalDateTime.of(date, times.end)
    )

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
    fun parse(html: String, currentYear: Int): ActivityDetails {
        val document = Jsoup.parse(html)
        val root = document.childNodes()[0] as Element
        val body = root.children()[1]
        val div = body.children().first()!!

        val dateString = div.select("p.smm-class-details__datetime").text()
        val dateRange = DateParser.parseDateTimeRange(dateString, currentYear)
        val buttonBook = div.select("button.book")
        return if (buttonBook.hasAttr("data-book-success")) {
            val json = buttonBook.attr("data-book-success")
            val data = kotlinxSerializer.decodeFromString<ActivityBookDataJson>(json)
            ActivityDetails(
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
            ActivityDetails(
                name = data.`class`.name.trim(),
                dateTimeRange = dateRange,
                venueName = data.venue.name.trim(),
                category = data.`class`.category.trim(),
                spotsLeft = 0,
            )
        }
    }

    fun parseFreetraining(html: String, year: Int): FreetrainingDetails {
        val document = Jsoup.parse(html)
        val root = document.childNodes()[0] as Element
        val body = root.children()[1]
        val div = body.children().first()!!

        return FreetrainingDetails(
            id = div.attr("data-appointment-id").toInt(),
            name = div.select("div.general h3").first()!!.text().trim(),
            date = div.select("p.smm-class-details__datetime").text().let { DateParser.parseDate(it, year) },
            category = div.select("span.disciplines").parents().first()!!.text().trim(),
            venueSlug = parseSlugFromGoogleMapUrls(div.select("div.usc-google-map").attr("data-static-map-urls")),
        )
    }

    private fun parseSlugFromGoogleMapUrls(jsonString: String): String {
        val url = kotlinxSerializer.decodeFromString<List<GoogleMapUrl>>(jsonString).first().url
        val fileName = url.replace("%2F", "/").substringAfterLast("/").substringBeforeLast("?")
        // staticMapMedium_1280x1280_amsterdam_13834_vitality-spa-fitness-amsterdam_172647253741728.png
        val parts = fileName.split("_")
        // [staticMapMedium, 1280x1280, amsterdam, 13834, vitality-spa-fitness-amsterdam, 172647253741728.png]
        return parts[4]
    }
}

/**
 * {"width":1280,"url":"https://storage.googleapis.com/download/storage/v1/b/usc-pro-uscweb-live-media/o/de-live%2FstaticMapMedium_1280x1280_amsterdam_13834_vitality-spa-fitness-amsterdam_172647253741728.png?generation=1726472537636021&alt=media"},
 * {"width":640,"url":"https://storage.googleapis.com/download/storage/v1/b/usc-pro-uscweb-live-media/o/de-live%2FstaticMapSmall_640x640_amsterdam_13834_vitality-spa-fitness-amsterdam_172647253741728.png?generation=1726472537337492&alt=media"}
 */
@Serializable
data class GoogleMapUrl(
    val width: Int,
    val url: String,
)
