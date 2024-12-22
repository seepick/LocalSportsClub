package seepick.localsportsclub.api.venue

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.fetchPageable
import seepick.localsportsclub.kotlinxSerializer
import seepick.localsportsclub.service.ApiException
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.requireStatusOk
import java.io.File
import java.net.ConnectException

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
    private val storeResponses: Boolean,
) : VenueApi {

    private val log = logger {}

    override suspend fun fetchPages(filter: VenuesFilter): List<VenuesJsonPage> =
        fetchPageable { fetchPage(filter, it) }

    // GET https://urbansportsclub.com/nl/venues?city_id=1144&plan_type=3&page=2
    private suspend fun fetchPage(filter: VenuesFilter, page: Int): VenuesJsonPage {
        val fullUrl = "$baseUrl/venues"
        log.debug { "Fetching venue page $page from: $fullUrl?page=$page&city_id=${filter.city.id}&plan_type=${filter.plan.id}" }
        val response = http.get(fullUrl) {
            cookie("PHPSESSID", phpSessionId)
            header("x-requested-with", "XMLHttpRequest") // IMPORTANT! to change the response to JSON!!!
            parameter("city_id", filter.city.id)
            parameter("plan_type", filter.plan.id)
            parameter("page", page)
        }
        response.requireStatusOk()

        val json = if (storeResponses) {
            val target = File(FileResolver.resolve(DirectoryEntry.ApiLogs), "VenuePage-$page.json")
            val responseText = response.bodyAsText()
            target.writeText(responseText)
            kotlinxSerializer.decodeFromString<VenuesJson>(responseText)
        } else {
            response.body<VenuesJson>()
        }
        if (!json.success) {
            throw ApiException("Venues endpoint returned failure!")
        }
        return json.data
    }

    override suspend fun fetchDetails(slug: String): VenueDetails {
        log.debug { "Fetching details for: [$slug]" }
        val fullUrl = "$baseUrl/venues/$slug"
        val response = try {
            http.get(fullUrl) {
                cookie("PHPSESSID", phpSessionId)
            }
        } catch (e: ConnectException) {
            e.printStackTrace()
            error("Failed to load details for: $fullUrl")
        }
        response.requireStatusOk()
        return VenueDetailsParser.parse(response.bodyAsText())
    }
}
