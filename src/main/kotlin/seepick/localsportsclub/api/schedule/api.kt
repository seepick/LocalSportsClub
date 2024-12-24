package seepick.localsportsclub.api.schedule

import io.ktor.client.HttpClient
import io.ktor.client.request.cookie
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import seepick.localsportsclub.service.safeGet

interface ScheduleApi {
    suspend fun fetchActivityIds(): List<Int>
}

class ScheduleHttpApi(
    private val http: HttpClient,
    private val baseUrl: String,
    private val phpSessionId: String
) : ScheduleApi {

    override suspend fun fetchActivityIds(): List<Int> {
        val response = http.safeGet(Url("$baseUrl/profile/schedule")) {
            cookie("PHPSESSID", phpSessionId)
        }
        return ScheduleParser.parse(response.bodyAsText()).activityIds
    }
}
