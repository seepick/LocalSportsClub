package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.kotest.common.runBlocking
import io.kotest.matchers.types.shouldBeInstanceOf
import seepick.localsportsclub.api.venue.VenueHttpApi
import seepick.localsportsclub.api.venue.VenuesFilter
import seepick.localsportsclub.service.httpClient

object ManualSystemTests {

    private val log = logger {}
    private const val baseUrl = "https://urbansportsclub.com/en"

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            log.info { "Manual test running..." }
            manualTest()
        }
    }

    private suspend fun manualTest() {
        val phpSessionId = System.getProperty("phpSessionId") ?: login().phpSessionId
//        println("phpSessionId: $phpSessionId")
        val venues = VenueHttpApi(httpClient, baseUrl, phpSessionId, false).fetchPages(
            VenuesFilter(
                city = City.Amsterdam,
                plan = PlanType.Large
            )
        )
        println("venue pages: ${venues.size}")
    }

    private suspend fun login(): LoginResult.Success {
        val username = System.getProperty("username") ?: error("Define: -Dusername=xxx")
        val password = System.getProperty("password") ?: error("Define: -Dpassword=xxx")
        val api = LoginApi(httpClient, baseUrl)
        val result = api.login(Credentials(username, password))
        return result.shouldBeInstanceOf<LoginResult.Success>()
    }
}
