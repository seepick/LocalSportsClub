package seepick.localsportsclub.sync.thirdparty

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.DateParser
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.httpClient
import seepick.localsportsclub.service.safeGet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HotFlowYogaVenue(
    val apiId: String,
    override val slug: String,
) : ThirdVenue {
    Jordaan("_WO5Hk", "hot-flow-yoga-jordaan"),
    Rivierenbuurt("MQYBtq", "hot-flow-yoga-rivierenbuurt"),
    Zuid("i102x0", "hot-flow-yoga-zuid"),
}

class HotFlowYogaFetcher(
    private val httpClient: HttpClient,
    private val clock: Clock,
) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                val events = HotFlowYogaFetcher(httpClient, SystemClock).fetch(HotFlowYogaVenue.Jordaan)
                println("events.size=${events.size}")
                events.forEach {
                    println("  - $it")
                }
            }
        }
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

    suspend fun fetch(venue: HotFlowYogaVenue): List<ThirdEvent> =
        fetchSingle(venue, clock.today()) + fetchSingle(venue, clock.today().plusDays(7))

    private suspend fun fetchSingle(venue: HotFlowYogaVenue, date: LocalDate): List<ThirdEvent> {
        val response = httpClient.safeGet(Url("https://www.eversports.nl/widget/api/eventsession/calendar")) {
            // ?facilityShortId=i102x0&startDate=2025-01-13&activeEventType=universal
            parameter("facilityShortId", venue.apiId)
            parameter("startDate", dateFormatter.format(date))
            parameter("activeEventType", "universal")
        }
        val json = response.body<HotFlowYogaJson>()
        require(json.status == "success") { "Invalid response status [${json.status}]" }
        return HotFlowYogaParser.parse(json.data.html)
    }
}

@Serializable
data class HotFlowYogaJson(
    val status: String, //  "success"
    val data: HotFlowYogaDataJson
)

@Serializable
data class HotFlowYogaDataJson(
    val html: String,
    val dateRange: String, // "13/01/2025 - 19/01/2025"
    // val navigation ...
)

object HotFlowYogaParser {

    private const val TIME_DELIMITER = "●"

    fun parse(htmlString: String): List<ThirdEvent> {
        val document = Jsoup.parse(htmlString)
        val html = document.childNodes()[0] as Element
        val body = html.children()[1]
        val rootDiv = body.children()
        return rootDiv.select("ul.calendar__slot-list").mapNotNull { ul ->
            val firstDiv = ul.select("div").first()!!
            if (!firstDiv.hasAttr("data-day")) {
                null
            } else {
                val date = DateParser.parseMachineDate(firstDiv.attr("data-day"))
                ul.select("li").mapNotNull { li ->
                    val teacher = li.select("div.ellipsis").last()!!.text().trim()
                    if (teacher == "Cancelled") {
                        null
                    } else {
                        ThirdEvent(
                            title = li.select("div.session-name").text().trim(),
                            teacher = teacher,
                            dateTimeRange = parseTime(date, li.select("div.session-time").text().trim()),
                        )
                    }
                }
            }
        }.flatten()
    }

    /** @param rawTime e.g. "12:00 ● 60 Min" */
    private fun parseTime(date: LocalDate, rawTime: String): DateTimeRange {
        val (timeString, durationString) = rawTime.split(TIME_DELIMITER)
        val time = DateParser.parseTime(timeString.trim())
        val from = LocalDateTime.of(date, time)
        val to = from.plusMinutes(durationString.replace("Min", "").trim().toLong())
        return DateTimeRange(from = from, to = to)
    }
}
