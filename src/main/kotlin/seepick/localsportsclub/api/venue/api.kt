package seepick.localsportsclub.api.venue

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.cookie
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PhpSessionId
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.ResponseStorage
import seepick.localsportsclub.api.UscConfig
import seepick.localsportsclub.api.fetchPageable
import seepick.localsportsclub.service.ApiException
import seepick.localsportsclub.service.safeGet

data class VenuesFilter(
    val city: City,
    val plan: PlanType,
)

interface VenueApi {
    suspend fun fetchPages(filter: VenuesFilter): List<VenuesDataJson>
    suspend fun fetchDetails(slug: String): VenueDetails
}

class VenueHttpApi(
    private val http: HttpClient,
    private val phpSessionId: PhpSessionId,
    private val responseStorage: ResponseStorage,
    uscConfig: UscConfig,
) : VenueApi {
    private val baseUrl = uscConfig.baseUrl

    private val log = logger {}

    override suspend fun fetchPages(filter: VenuesFilter): List<VenuesDataJson> =
        fetchPageable { fetchPage(filter, it) }

    // GET https://urbansportsclub.com/nl/venues?city_id=1144&plan_type=3&page=2
    private suspend fun fetchPage(filter: VenuesFilter, page: Int): VenuesDataJson {
        log.debug { "Fetching venue page $page" }
        val response = http.safeGet(Url("$baseUrl/venues")) {
            cookie("PHPSESSID", phpSessionId.value)
            header("x-requested-with", "XMLHttpRequest") // IMPORTANT! to change the response to JSON!!!
            parameter("city_id", filter.city.id)
            parameter("plan_type", filter.plan.id)
            parameter("page", page)
        }
        responseStorage.store(response, "VenuesPage-$page")
        val json = response.body<VenuesJson>()
        if (!json.success) {
            throw ApiException("Venues endpoint returned failure!")
        }
        return json.data
    }

    override suspend fun fetchDetails(slug: String): VenueDetails {
        log.debug { "Fetching details for: [$slug]" }
        val response = http.safeGet(Url("$baseUrl/venues/$slug")) {
            cookie("PHPSESSID", phpSessionId.value)
        }
        responseStorage.store(response, "VenueDetails-$slug")
        return VenueDetailsParser.parse(response.bodyAsText())
    }
}
