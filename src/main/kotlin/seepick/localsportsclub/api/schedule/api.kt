package seepick.localsportsclub.api.schedule

import io.ktor.client.HttpClient
import io.ktor.client.request.cookie
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.api.PhpSessionId
import seepick.localsportsclub.service.safeGet

interface ScheduleApi {
    suspend fun fetchActivityIds(): List<Int>
}

class ScheduleHttpApi(
    private val http: HttpClient,
    private val phpSessionId: PhpSessionId,
    uscConfig: UscConfig,
) : ScheduleApi {

    private val baseUrl = uscConfig.baseUrl

    override suspend fun fetchActivityIds(): List<Int> {
        val response = http.safeGet(Url("$baseUrl/profile/schedule")) {
            cookie("PHPSESSID", phpSessionId.value)
        }
        return ScheduleParser.parse(response.bodyAsText()).activityIds
    }
}
