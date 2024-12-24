package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.kotest.common.runBlocking
import io.kotest.matchers.types.shouldBeInstanceOf
import seepick.localsportsclub.UscConfig
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
    private val uscConfig = UscConfig(
        city = City.Amsterdam,
        plan = PlanType.Large,
        storeResponses = false,
    )

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
        val pages = VenueHttpApi(httpClient, PhpSessionId(phpSessionId), uscConfig).fetchPages(
            VenuesFilter(
                city = City.Amsterdam,
                plan = PlanType.Large
            )
        )
        println("venue pages: ${pages.size}")
    }

    private suspend fun testActivities(phpSessionId: PhpSessionId) {
        val today = LocalDate.now()
        val pages = ActivityHttpApi(httpClient, phpSessionId, uscConfig).fetchPages(
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

    private suspend fun testSchedule(phpSessionId: PhpSessionId) {
        val ids = ScheduleHttpApi(httpClient, phpSessionId, uscConfig).fetchActivityIds()
        println("Got ${ids.size} activity IDs back: $ids")
    }

    private suspend fun getSessionId(): PhpSessionId {
        val syspropSessionId = System.getProperty("phpSessionId")
        if (syspropSessionId != null) {
            println("Using system property's session ID: $syspropSessionId")
            return PhpSessionId(syspropSessionId)
        }
        val syspropUsername = System.getProperty("username")
        val syspropPassword = System.getProperty("password")
        val credentials = if (syspropUsername != null && syspropPassword != null) {
            Credentials(syspropUsername, syspropPassword)
        } else {
            Credentials.load()
        }
        return LoginApi(httpClient, uscConfig.baseUrl)
            .login(credentials)
            .shouldBeInstanceOf<LoginResult.Success>()
            .phpSessionId.let {
                println("New session ID is: $it")
                PhpSessionId(it)
            }
    }

}
