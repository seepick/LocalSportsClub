package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.kotest.common.runBlocking
import io.kotest.matchers.types.shouldBeInstanceOf
import seepick.localsportsclub.api.activities.ActivitiesFilter
import seepick.localsportsclub.api.activities.ActivitiesParser
import seepick.localsportsclub.api.activities.ActivityHttpApi
import seepick.localsportsclub.api.activities.ServiceTye
import seepick.localsportsclub.api.schedule.ScheduleHttpApi
import seepick.localsportsclub.api.venue.VenueHttpApi
import seepick.localsportsclub.api.venue.VenuesFilter
import seepick.localsportsclub.service.httpClient
import java.time.LocalDate

object ManualSystemTests {

    private val log = logger {}
    private const val baseUrl = "https://urbansportsclub.com/en"

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            log.info { "Manual test running..." }
            val phpSessionId = getSessionId()
//            testVenues(phpSessionId)
//            testActivities(phpSessionId)
            testSchedule(phpSessionId)
        }
    }

    private suspend fun testVenues(phpSessionId: String) {
        val pages = VenueHttpApi(httpClient, baseUrl, phpSessionId, false).fetchPages(
            VenuesFilter(
                city = City.Amsterdam,
                plan = PlanType.Large
            )
        )
        println("venue pages: ${pages.size}")
    }

    private suspend fun testActivities(phpSessionId: String) {
        val today = LocalDate.now()
        val pages = ActivityHttpApi(httpClient, baseUrl, phpSessionId).fetchPages(
            ActivitiesFilter(
                city = City.Amsterdam, plan = PlanType.Large, date = today, service = ServiceTye.Courses
            )
        )
        println("Received ${pages.size} pages of activities.")
        val activities = pages.flatMapIndexed { index, page ->
            println("Parsing page ${index + 1}")
            ActivitiesParser.parse(page.content, today)
        }
        println("In total ${activities.size} activities:")
        activities.forEach { println("- $it") }
    }

    private suspend fun testSchedule(phpSessionId: String) {
        val ids = ScheduleHttpApi(httpClient, baseUrl, phpSessionId).fetchActivityIds()
        println("Got ${ids.size} activity IDs back: $ids")
    }

    private suspend fun getSessionId(): String {
        val syspropSessionId = System.getProperty("phpSessionId")
        if (syspropSessionId != null) {
            println("Using system property's session ID: $syspropSessionId")
            return syspropSessionId
        }
        val syspropUsername = System.getProperty("username")
        val syspropPassword = System.getProperty("password")
        val credentials = if (syspropUsername != null && syspropPassword != null) {
            Credentials(syspropUsername, syspropPassword)
        } else {
            Credentials.load()
        }
        return LoginApi(httpClient, baseUrl)
            .login(credentials)
            .shouldBeInstanceOf<LoginResult.Success>()
            .phpSessionId.also {
                println("New session ID is: $it")
            }
    }

}
