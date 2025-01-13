package seepick.localsportsclub.sync.thirdparty

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.jsoup.nodes.Element
import seepick.localsportsclub.api.NoopResponseStorage
import seepick.localsportsclub.api.ResponseStorage
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.DateParser
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.date.daysBetween
import seepick.localsportsclub.service.httpClient
import seepick.localsportsclub.service.jsoupBody
import seepick.localsportsclub.service.safeGet
import java.time.LocalDate
import java.time.LocalDateTime

data class DeNieuweYogaSchoolFetcherRequest(
    val whichDaysToSync: List<LocalDate>
) : HasSlug {
    override val slug: String = "de-nieuwe-yogaschool"
}

class DeNieuweYogaSchoolFetcher(
    private val httpClient: HttpClient,
    private val responseStorage: ResponseStorage,
    private val clock: Clock,
) {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                val events = DeNieuweYogaSchoolFetcher(httpClient, NoopResponseStorage, SystemClock).fetch(
                    DeNieuweYogaSchoolFetcherRequest(
                        listOf(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4))
                    )
                )
                println("received ${events.size} events:")
                events.forEach {
                    println("- $it")
                }
            }
        }
    }

    private val log = logger {}

    suspend fun fetch(request: DeNieuweYogaSchoolFetcherRequest): List<ThirdEvent> {
        log.debug { "Sync DeNieuweYogaSchool events for: ${request.whichDaysToSync}" }
        return request.whichDaysToSync
            .map { clock.today().daysBetween(it) }
            .flatMap { dayOffset ->
                fetchSingle(dayOffset)
            }
    }

    private suspend fun fetchSingle(dayOffset: Long): List<ThirdEvent> {
        val response = httpClient.safeGet(Url("https://denieuweyogaschool.nl/club_portal/lessons/$dayOffset"))
        responseStorage.store(response, "DeNieuweYogaSchool-$dayOffset")
        return DeNieuweYogaSchoolParser.parse(response.bodyAsText())
    }
}

object DeNieuweYogaSchoolParser {

    private val log = logger {}

    fun parse(htmlString: String): List<ThirdEvent> =
        jsoupBody(htmlString).select("li.lesson_element").map { lesson ->
            parseEvent(lesson)
        }

    private fun parseEvent(lesson: Element): ThirdEvent =
        ThirdEvent(
            title = lesson.select("div.headlesson h3").text().trim(),
            teacher = lesson.select("div.headlesson span.trainer").text().substringAfter("gegeven door").trim(),
            dateTimeRange = parseDateTimeRange(lesson.select("span.givenby").text())
        )

    // 14 januari 2025 18:00- 19:00 UUR
    private fun parseDateTimeRange(string: String): DateTimeRange {
        log.trace { "parseDateTimeRange($string)" }
        val parts = string.split(" ")
        val date = DateParser.parseDutchDate(parts[0] + " " + parts[1] + " " + parts[2])
        val timeParts = (3..<parts.size).joinToString("") { parts[it] }.replace("UUR", "").trim().split("-")
        require(timeParts.size == 2)
        val fromTimePart = timeParts[0]
        val toTimePart = timeParts[1]
        return DateTimeRange(
            from = LocalDateTime.of(date, DateParser.parseTime(fromTimePart)),
            to = LocalDateTime.of(date, DateParser.parseTime(toTimePart)),
        )
    }
}
