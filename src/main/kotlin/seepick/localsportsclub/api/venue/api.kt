package seepick.localsportsclub.api.venue

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import seepick.localsportsclub.api.ApiException
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.fetchPageable
import seepick.localsportsclub.api.requireStatusOk

data class VenueRequest(
    val city: City,
    val plan: PlanType,
)

interface VenueApi {
    suspend fun fetchPages(request: VenueRequest): List<VenuesJsonPage>
}

class VenueHttpApi(
    private val http: HttpClient,
    private val baseUrl: String,
    private val phpSessionId: String,
) : VenueApi {

    override suspend fun fetchPages(request: VenueRequest): List<VenuesJsonPage> =
        fetchPageable { fetchPage(request, it) }

    // GET https://urbansportsclub.com/nl/venues?city_id=1144&plan_type=3&page=2
    private suspend fun fetchPage(request: VenueRequest, page: Int): VenuesJsonPage {
        val response = http.get("$baseUrl/venues") {
            cookie("PHPSESSID", phpSessionId)
            header("x-requested-with", "XMLHttpRequest") // IMPORTANT! to change the response to JSON!!!
            parameter("city_id", request.city.id)
            parameter("plan_type", request.plan.id)
            parameter("page", page)
        }
        response.requireStatusOk()

        val json = response.body<VenuesJson>()
        if (!json.success) {
            throw ApiException("Venues endpoint returned failure!")
        }
        return json.data
    }
}
