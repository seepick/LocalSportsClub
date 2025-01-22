package seepick.localsportsclub.sync.thirdparty

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import seepick.localsportsclub.api.NoopResponseStorage
import seepick.localsportsclub.api.ResponseStorage
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.DateParser
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.date.machinePrint
import seepick.localsportsclub.service.httpClient
import seepick.localsportsclub.service.safeGet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HotFlowYogaStudio(
    override val eversportsId: String,
    override val slug: String,
) : HasSlug, EversportsFetchRequest {
    Jordaan("_WO5Hk", "hot-flow-yoga-jordaan"),
    Rivierenbuurt("MQYBtq", "hot-flow-yoga-rivierenbuurt"),
    Zuid("i102x0", "hot-flow-yoga-zuid");

    override val logId = slug
}

interface EversportsFetchRequest {
    val eversportsId: String
    val logId: String
}

data class EversportsFetchRequestImpl(
    override val eversportsId: String,
    override val slug: String,
) : EversportsFetchRequest, HasSlug {
    override val logId = slug
}

class EversportsFetcher(
    private val httpClient: HttpClient,
    private val responseStorage: ResponseStorage,
    private val clock: Clock,
) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                val events =
                    EversportsFetcher(httpClient, NoopResponseStorage, SystemClock)
                        .fetch(
                            EversportsFetchRequestImpl(
                                eversportsId = "J3pkIl",
                                slug = "movement-amsterdam",
//                                HotFlowYogaStudio.Jordaan.eversportsId,
//                                "hotflowyoga-jordaan"
                            )
                        )
                println("events.size=${events.size}")
                events.forEach {
                    println("  - $it")
                }
            }
        }
    }

    private val log = logger {}

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

    suspend fun fetch(request: EversportsFetchRequest): List<ThirdEvent> {
        log.debug { "Fetching eversports events for: ${request.logId}" }
        val today = clock.today()
        return fetchSingle(request, today) + fetchSingle(request, today.plusDays(7))
    }

    private suspend fun fetchSingle(request: EversportsFetchRequest, date: LocalDate): List<ThirdEvent> {
        val response = httpClient.safeGet(Url("https://www.eversports.nl/widget/api/eventsession/calendar")) {
            parameter("facilityShortId", request.eversportsId)
            parameter("startDate", dateFormatter.format(date))
            parameter("activeEventType", "universal")
        }
        responseStorage.store(response, "${request.logId}-${date.machinePrint()}")
        val json = response.body<EversportRootJson>()
        require(json.status == "success") { "Invalid response status [${json.status}]" }
        return EversportsParser.parse(json.data.html)
    }
}


@Serializable
data class EversportRootJson(
    val status: String, //  "success"
    val data: EversportsDataJson
)

@Serializable
data class EversportsDataJson(
    val html: String,
    val dateRange: String, // "13/01/2025 - 19/01/2025"
    // val navigation ...
)

object EversportsParser {

    private const val TIME_DELIMITER = "●"

    fun parse(htmlString: String): List<ThirdEvent> {
        val document = Jsoup.parse(htmlString)
        val html = document.childNodes()[0] as Element
        val body = html.children()[1]
        val rootDiv = body.children()
        return rootDiv.select("ul.calendar__slot-list").mapNotNull { ul ->
            val firstDiv = ul.select("div").first()
            if (firstDiv == null || !firstDiv.hasAttr("data-day")) {
                null
            } else {
                val date = DateParser.parseMachineDate(firstDiv.attr("data-day"))
                ul.select("li").mapNotNull { li ->
                    val lastEllipsis = li.select("div.ellipsis").last()!!
                    if (lastEllipsis.children().size == 0) {
                        // contains no sub-html nodes, thus it must be the teacher (and not the price/level/spots info node)
                        val teacher = lastEllipsis.text().trim()
                        if (teacher == "Cancelled") {
                            null
                        } else {
                            ThirdEvent(
                                title = li.select("div.session-name").text().trim(),
                                teacher = teacher,
                                dateTimeRange = parseTime(date, li.select("div.session-time").text().trim()),
                            )
                        }
                    } else {
                        null
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
