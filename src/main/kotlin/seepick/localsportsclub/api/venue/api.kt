package seepick.localsportsclub.api.venue

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import seepick.localsportsclub.api.ApiException
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.fetchPageable
import seepick.localsportsclub.api.requireStatusOk
import seepick.localsportsclub.api.venueDetails.VenueDetails
import seepick.localsportsclub.api.venueDetails.VenueDetailsParser

data class VenuesFilter(
    val city: City,
    val plan: PlanType,
)

interface VenueApi {
    suspend fun fetchPages(filter: VenuesFilter): List<VenuesJsonPage>
    suspend fun fetchDetails(slug: String): VenueDetails
}

class VenueHttpApi(
    private val http: HttpClient,
    private val baseUrl: String,
    private val phpSessionId: String,
) : VenueApi {

    private val log = logger {}
    override suspend fun fetchPages(filter: VenuesFilter): List<VenuesJsonPage> =
        fetchPageable { fetchPage(filter, it) }

    // GET https://urbansportsclub.com/nl/venues?city_id=1144&plan_type=3&page=2
    private suspend fun fetchPage(filter: VenuesFilter, page: Int): VenuesJsonPage {
        log.debug { "Fetching page $page" }
        val response = http.get("$baseUrl/venues") {
            cookie("PHPSESSID", phpSessionId)
            header("x-requested-with", "XMLHttpRequest") // IMPORTANT! to change the response to JSON!!!
            parameter("city_id", filter.city.id)
            parameter("plan_type", filter.plan.id)
            parameter("page", page)
        }
        response.requireStatusOk()

        val json = response.body<VenuesJson>()
        if (!json.success) {
            throw ApiException("Venues endpoint returned failure!")
        }
        return json.data
    }

    override suspend fun fetchDetails(slug: String): VenueDetails {
        log.debug { "Fetching details for: [$slug]" }
        val response = http.get("$baseUrl/venues/$slug") {
            cookie("PHPSESSID", phpSessionId)
        }
        response.requireStatusOk()
        return VenueDetailsParser.parse(response.bodyAsText())
    }
}
