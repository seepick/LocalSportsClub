package seepick.localsportsclub.api.activities

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.request
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PhpSessionId
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.fetchPageable
import seepick.localsportsclub.service.ApiException
import seepick.localsportsclub.service.requireStatusOk
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface ActivityApi {
    suspend fun fetchPages(filter: ActivitiesFilter): List<ActivitiesDataJson>
}

class ActivityHttpApi(
    private val http: HttpClient,
    private val phpSessionId: PhpSessionId,
    uscConfig: UscConfig,
) : ActivityApi {

    private val log = logger {}
    private val baseUrl = uscConfig.baseUrl


    override suspend fun fetchPages(filter: ActivitiesFilter): List<ActivitiesDataJson> =
        fetchPageable { fetchPage(filter, it) }

    // /activities?service_type=0&city=1144&date=2024-12-16&business_type[]=b2c&plan_type=3&type[]=onsite&page=2
    private suspend fun fetchPage(filter: ActivitiesFilter, page: Int): ActivitiesDataJson {
        val fullUrl = "$baseUrl/activities"
        val response = http.get(fullUrl) {
            cookie("PHPSESSID", phpSessionId.value)
            header("x-requested-with", "XMLHttpRequest") // IMPORTANT! to change the response to JSON!!!
            parameter("city_id", filter.city.id)
            parameter("date", filter.date.format(DateTimeFormatter.ISO_LOCAL_DATE)) // 2024-12-16
            parameter("plan_type", filter.plan.id)
//            parameter("business_type[]", "b2c")
            parameter("type[]", ActivityType.OnSite.apiValue) // onsite or online
            parameter("service_type", filter.service.apiValue) // (scheduled) courses or free training (dropin)
            parameter("page", page)
        }
        log.debug { "Fetched activities page $page from: ${response.request.url}" }
        response.requireStatusOk()
        val json = response.body<ActivitiesJson>()
        if (!json.success) {
            throw ApiException("Activities endpoint returned failure!")
        }
        return json.data
    }
}

data class ActivitiesFilter(
    val city: City,
    val plan: PlanType,
    val date: LocalDate,
    val service: ServiceTye,
)

enum class ActivityType(val apiValue: String) {
    OnSite("onsite"), OnlineLive("live")
}

enum class ServiceTye(val apiValue: Int) {
    Courses(0),
    FreeTraining(1),
}
