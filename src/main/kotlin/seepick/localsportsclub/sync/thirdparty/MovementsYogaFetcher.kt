package seepick.localsportsclub.sync.thirdparty

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import seepick.localsportsclub.api.NoopResponseStorage
import seepick.localsportsclub.api.ResponseStorage
import seepick.localsportsclub.service.date.DateParser
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.service.httpClient
import seepick.localsportsclub.service.model.HasSlug
import seepick.localsportsclub.service.safeGet
import java.time.LocalDate
import java.time.LocalDateTime

enum class MovementsYogaStudio(
    val apiId: Int,
    override val slug: String,
) : HasSlug {
    Vondelpark(apiId = 1, slug = "movements-overtoom"),
    City(apiId = 3, slug = "movements-city")
}

class MovementsYogaFetcher(
    private val http: HttpClient,
    private val responseStorage: ResponseStorage,
) {
    private val log = logger {}

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                val events = MovementsYogaFetcher(
                    http = httpClient,
                    responseStorage = NoopResponseStorage,
                ).fetch(MovementsYogaStudio.City)
                println("received ${events.size} events:")
                events.forEach {
                    println("- $it")
                }
            }
        }
    }

    suspend fun fetch(studio: MovementsYogaStudio): List<ThirdEvent> {
        log.debug { "Fetching events for movements yoga for this and next 2 weeks..." }
        return fetchWeek(studio, 0) + fetchWeek(studio, 1) + fetchWeek(studio, 2)
    }

    private suspend fun fetchWeek(studio: MovementsYogaStudio, weekOffset: Int): List<ThirdEvent> {
        val response = http.safeGet(Url("https://movementsyoga.zingfit.com/reserve/index.cfm")) {
            parameter("action", "Reserve.chooseClass")
            parameter("wk", weekOffset)
            parameter("site", studio.apiId)
        }
        responseStorage.store(response, "MovementsYoga-${studio.slug}-$weekOffset")
        return MovementsYogaParser.parse(response.bodyAsText())
    }
}

object MovementsYogaParser {

    private val log = logger {}

    fun parse(htmlString: String): List<ThirdEvent> {
        val document = Jsoup.parse(htmlString)
        val html = document.childNodes().single { it.nodeName() == "html" }
        val body = html.childNodes().single { it.nodeName() == "body" } as Element
        return body.select("div.tab-pane").flatMap { dayDiv ->
            val date = parseDate(dayDiv.attr("id"))
            dayDiv.select("div.scheduleBlock").mapNotNull {
                parseSingle(date, it)
            }
        }
    }

    private fun parseSingle(date: LocalDate, eventDiv: Element): ThirdEvent? {
        val customType = eventDiv.select("i.icon-info-sign").attr("data-customtype")
        if (customType.isNotEmpty()) {
            return null // a day event/special workshop, not a regular class
        }
        val timeString = eventDiv.select("span.scheduleTime").text().trim()
        if (timeString.contains("cancelled")) {
            return null
        }
        if (eventDiv.classNames().contains("empty")) {
            // We do not currently have any Classes available this day.
            return null
        }
        val title = eventDiv.select("span.scheduleClass").text().trim()
        val durationText = eventDiv.select("span.classlength").text().trim()
            .replace("minutes", "")
            .replace("mins", "")
            .replace("min", "").trim()
        if (durationText.isEmpty()) {
            log.debug { "Skipping entry as duration text is empty: $eventDiv" }
            return null
        }
        val from = LocalDateTime.of(date, DateParser.parseTime(timeString))
        val to = from.plusMinutes(durationText.toLong())
        return ThirdEvent(
            title = title,
            dateTimeRange = DateTimeRange(
                from = from,
                to = to,
            ),
            teacher = eventDiv.select("span.scheduleInstruc").text().trim(),
        )
    }

    /** @param string e.g.: "day20250124" */
    private fun parseDate(string: String): LocalDate = DateParser.parseConcatDate(string.substringAfter("day"))
}
