package seepick.localsportsclub.api.activities

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import seepick.localsportsclub.kotlinxSerializer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private typealias TimePairs = Pair<Pair<Int, Int>, Pair<Int, Int>>

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
        val children = body.children()
        log.debug { "Parsing ${children.size} activities." }
        return children.map { div ->
            parseSingle(div, date)
        }
    }

    private fun parseSingle(div: Element, date: LocalDate): ActivityInfo {
        val dataLayerJsonString = div.select("a[href=\"#modal-class\"]").first()!!.attr("data-datalayer")
        val dataLayer = kotlinxSerializer.decodeFromString<ActivityDataLayerJson>(dataLayerJsonString).`class`
        val (from, to) = convertFromToDateTime(
            date, parseTime(div.select("p.smm-class-snippet__class-time").text())
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
    LocalDateTime.of(date, LocalTime.of(times.first.first, times.first.second)) to
            LocalDateTime.of(date, LocalTime.of(times.second.first, times.second.second))

private fun parseTime(text: String): TimePairs =
    text.split("—")
        .map { twoTimes ->
            twoTimes.trim().split(":").let { numberParts ->
                require(numberParts.size == 2) { "Expected to be 2 number parts: ${numberParts.size} ($text)" }
                numberParts[0].toInt() to numberParts[1].toInt()
            }
        }.let { twoTimesList ->
            require(twoTimesList.size == 2) { "Times list expected to be 2 but was: ${twoTimesList.size} ($text)" }
            twoTimesList[0] to twoTimesList[1]
        }
