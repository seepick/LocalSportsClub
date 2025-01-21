package seepick.localsportsclub.api.activity

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.cookie
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import seepick.localsportsclub.api.PhpSessionId
import seepick.localsportsclub.api.ResponseStorage
import seepick.localsportsclub.api.UscConfig
import seepick.localsportsclub.api.fetchPageable
import seepick.localsportsclub.service.ApiException
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.Plan
import seepick.localsportsclub.service.safeGet
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface ActivityApi {
    suspend fun fetchPages(
        session: PhpSessionId,
        filter: ActivitiesFilter,
        serviceType: ServiceType
    ): List<ActivitiesDataJson>

    suspend fun fetchDetails(session: PhpSessionId, id: Int): ActivityDetails
    suspend fun fetchFreetrainingDetails(session: PhpSessionId, id: Int): FreetrainingDetails
}

data class ActivitiesFilter(
    val city: City,
    val plan: Plan,
    val date: LocalDate,
)

enum class ActivityType(val apiValue: String) {
    OnSite("onsite"), OnlineLive("live")
}

enum class ServiceType(val apiValue: Int) {
    Courses(0),
    FreeTraining(1),
}

data class ActivityDetails(
    val name: String,
    val dateTimeRange: DateTimeRange,
    val venueName: String,
    val category: String,
    val spotsLeft: Int,
)

data class FreetrainingDetails(
    val id: Int,
    val name: String,
    val date: LocalDate,
    val venueSlug: String,
    val category: String,
)

class ActivityHttpApi(
    private val http: HttpClient,
    private val responseStorage: ResponseStorage,
    uscConfig: UscConfig,
    private val clock: Clock,
) : ActivityApi {

    private val log = logger {}
    private val baseUrl = uscConfig.baseUrl

    override suspend fun fetchPages(
        session: PhpSessionId,
        filter: ActivitiesFilter,
        serviceType: ServiceType
    ): List<ActivitiesDataJson> =
        fetchPageable { fetchPage(session, filter, serviceType, it) }

    // /activities?service_type=0&city=1144&date=2024-12-16&business_type[]=b2c&plan_type=3&type[]=onsite&page=2
    private suspend fun fetchPage(
        session: PhpSessionId,
        filter: ActivitiesFilter,
        serviceType: ServiceType,
        page: Int
    ): ActivitiesDataJson {
        val response = http.safeGet(Url("$baseUrl/activities")) {
            cookie("PHPSESSID", session.value)
            header("x-requested-with", "XMLHttpRequest") // IMPORTANT! to change the response to JSON!!!
            parameter("city_id", filter.city.id)
            parameter("date", filter.date.format(DateTimeFormatter.ISO_LOCAL_DATE)) // 2024-12-16
            parameter("plan_type", filter.plan.id)
//            parameter("business_type[]", "b2c")
            parameter("type[]", ActivityType.OnSite.apiValue) // onsite or online
            parameter("service_type", serviceType.apiValue) // (scheduled) courses or free training (dropin)
            parameter("page", page)
        }
        responseStorage.store(response, "ActivtiesPage-$page")
        val json = response.body<ActivitiesJson>()
        if (!json.success) {
            throw ApiException("Activities endpoint returned failure!")
        }
        return json.data
    }

    override suspend fun fetchDetails(session: PhpSessionId, id: Int): ActivityDetails {
        log.debug { "Fetching details for $id" }
        val response = http.safeGet(Url("$baseUrl/class-details/$id")) {
            cookie("PHPSESSID", session.value)
        }
        responseStorage.store(response, "ActivtiesDetails-$id")
        return ActivityParser.parse(response.bodyAsText(), clock.today().year)
    }

    override suspend fun fetchFreetrainingDetails(session: PhpSessionId, id: Int): FreetrainingDetails {
        val response = http.safeGet(Url("$baseUrl/class-details/$id")) {
            cookie("PHPSESSID", session.value)
        }
        responseStorage.store(response, "FreetrainingDetails-$id")
        return ActivityParser.parseFreetraining(response.bodyAsText(), clock.today().year)
    }
}
