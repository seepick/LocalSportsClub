package seepick.localsportsclub.api.activities

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.StatsDistrictJson
import seepick.localsportsclub.api.StatsJson
import seepick.localsportsclub.kotlinxSerializer
import seepick.localsportsclub.toFlatMap
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ActivityHttpApiTest : StringSpec() {
    private val baseUrl = "https://test"
    private val phpSessionId = "testPhpSessionId"
    private val filter = ActivitiesFilter(
        city = City.Amsterdam,
        plan = PlanType.Medium,
        date = LocalDate.of(2024, 12, 1),
        service = ServiceTye.Courses,
    )

    init {
        "Given data returned When fetch page Then return data" {
            val rootJson = buildActivitiesJson(success = true, showMore = false)
            val expectedUrl =
                "$baseUrl/venues?city_id=${filter.city.id}&date=${filter.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}&plan_type=${filter.plan.id}&type%5B%5D=${ActivityType.OnSite.apiValue}&service_type=${filter.service.apiValue}&page=1"
            val http =
                buildMockClient(expectedUrl = expectedUrl, phpSessionId = phpSessionId, responsePayload = rootJson)
            val api = ActivityHttpApi(http = http, baseUrl = baseUrl, phpSessionId = phpSessionId)

            val response = api.fetchPages(filter)

            response.shouldBeSingleton().first() shouldBe rootJson.data
        }
    }
}

inline fun <reified T> buildMockClient(expectedUrl: String, phpSessionId: String, responsePayload: T) =
    HttpClient(MockEngine { request ->
        request.url.toString() shouldBe expectedUrl
        val headers = request.headers.toFlatMap()
        headers.shouldContain("x-requested-with" to "XMLHttpRequest")
        headers["Cookie"].shouldContain("PHPSESSID=$phpSessionId")
        respond(
            content = kotlinxSerializer.encodeToString(responsePayload),
            status = HttpStatusCode.OK,
            headers = Headers.build {
                append("Content-Type", "application/json")
            })
    }) {
        install(ContentNegotiation) {
            json(kotlinxSerializer)
        }
    }

private fun buildActivitiesJson(success: Boolean, showMore: Boolean) = ActivitiesJson(
    success = success, data = ActivitiesDataJson(
        showMore = showMore, content = "HTML", stats = StatsJson(
            category = listOf(),
            district = StatsDistrictJson(district = listOf(), areas = listOf()),
            venue = listOf()
        ), emptySnippet = null, searchExecutedEvent = "{}", regionSelectorSelected = null
    )
)