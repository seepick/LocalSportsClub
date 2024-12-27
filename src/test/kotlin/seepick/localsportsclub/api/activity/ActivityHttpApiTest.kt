package seepick.localsportsclub.api.activity

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import seepick.localsportsclub.TestableClock
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.NoopResponseStorage
import seepick.localsportsclub.api.PhpSessionId
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.StatsDistrictJson
import seepick.localsportsclub.api.StatsJson
import seepick.localsportsclub.kotlinxSerializer
import seepick.localsportsclub.toFlatMap
import seepick.localsportsclub.uscConfig
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ActivityHttpApiTest : StringSpec() {
    private val uscConfig = Arb.uscConfig().next()
    private val phpSessionId = PhpSessionId("testPhpSessionId")
    private val clock = TestableClock()
    private val filter = ActivitiesFilter(
        city = City.Amsterdam,
        plan = PlanType.Medium,
        date = LocalDate.of(2024, 12, 1),
    )

    init {
        "Given data returned When fetch page Then return data" {
            val rootJson = buildActivitiesJson(success = true, showMore = false)
            val expectedUrl =
                "${uscConfig.baseUrl}/activities?city_id=${filter.city.id}&date=${filter.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}&plan_type=${filter.plan.id}&type%5B%5D=${ActivityType.OnSite.apiValue}&service_type=${ServiceTye.Courses.apiValue}&page=1"
            val http =
                buildMockClient(expectedUrl = expectedUrl, phpSessionId = phpSessionId, responsePayload = rootJson)
            val api = ActivityHttpApi(
                http = http,
                phpSessionId = phpSessionId,
                uscConfig = uscConfig,
                clock = clock,
                responseStorage = NoopResponseStorage
            )

            val response = api.fetchPages(filter, ServiceTye.Courses)

            response.shouldBeSingleton().first() shouldBe rootJson.data
        }
    }
}

inline fun <reified T> buildMockClient(expectedUrl: String, phpSessionId: PhpSessionId, responsePayload: T) =
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
