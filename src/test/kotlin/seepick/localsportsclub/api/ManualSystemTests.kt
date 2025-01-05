package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.kotest.common.runBlocking
import io.kotest.matchers.types.shouldBeInstanceOf
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.api.activity.ActivitiesFilter
import seepick.localsportsclub.api.activity.ActivitiesParser
import seepick.localsportsclub.api.activity.ActivityHttpApi
import seepick.localsportsclub.api.activity.ServiceType
import seepick.localsportsclub.api.checkin.CheckinHttpApi
import seepick.localsportsclub.api.schedule.ScheduleHttpApi
import seepick.localsportsclub.api.venue.VenueHttpApi
import seepick.localsportsclub.api.venue.VenueParser
import seepick.localsportsclub.api.venue.VenuesFilter
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.httpClient
import java.time.LocalDate

object ManualSystemTests {

    private val log = logger {}
    private val uscConfig = UscConfig(
        city = City.Amsterdam,
        plan = PlanType.Large,
        storeResponses = false,
    )
    private val responseStorage = ResponseStorageImpl()
    private val phpSessionId: PhpSessionId by lazy { runBlocking { loadSessionId() } }

    // https://urbansportsclub.com/en/venues/wilhelmina-gasthuisterrein
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            log.info { "Manual test running..." }
//            testFreetrainingDetails()
//            testCheckins()
//            testVenues()
            testVenue()
//            testActivities()
//            testSchedule()
        }
    }

    private fun activityApi() = ActivityHttpApi(httpClient, phpSessionId, responseStorage, uscConfig, SystemClock)
    private fun checkinApi() = CheckinHttpApi(httpClient, phpSessionId, responseStorage, SystemClock, uscConfig)
    private fun venueApi() = VenueHttpApi(httpClient, phpSessionId, responseStorage, uscConfig)

    private suspend fun testFreetrainingDetails() {
        val freetrainingId = 83664090
        val result = activityApi().fetchFreetrainingDetails(freetrainingId)
        println(result)
    }

    private suspend fun testCheckins() {
        val response = checkinApi().fetchPage(1)
        println("received ${response.entries.size} checkins")
        response.entries.forEach { entry ->
            println(entry)
        }
    }

    private suspend fun testVenue() {
        val details = venueApi().fetchDetails("wilhelmina-gasthuisterrein")
        println("details.title=${details.title}")
        println("details.websiteUrl=${details.websiteUrl}")
    }

    private suspend fun testVenues() {
        val pages = venueApi().fetchPages(
            VenuesFilter(
                city = City.Amsterdam, plan = PlanType.Large
            )
        )
        pages.flatMap { VenueParser.parseHtmlContent(it.content) }.sortedBy { it.slug }
            .also { println("Received ${it.size} venues (without those missing from linkings)") }.forEach(::println)
    }

    private suspend fun testActivities() {
        val today = LocalDate.now()
        val pages = activityApi().fetchPages(
            filter = ActivitiesFilter(city = City.Amsterdam, plan = PlanType.Large, date = today),
            serviceType = ServiceType.Courses,
        )
        println("Received ${pages.size} pages of activities.")
        val activities = pages.flatMapIndexed { index, page ->
            println("Parsing page ${index + 1}")
            ActivitiesParser.parseContent(page.content, today)
        }
        println("In total ${activities.size} activities:")
        activities.forEach { println("- $it") }
    }

    private suspend fun testSchedule() {
        val ids = ScheduleHttpApi(httpClient, phpSessionId, responseStorage, uscConfig).fetchScheduleRows()
        println("Got ${ids.size} activity IDs back: $ids")
    }

    private suspend fun loadSessionId(): PhpSessionId {
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
        return LoginApi(httpClient, uscConfig.baseUrl).login(credentials)
            .shouldBeInstanceOf<LoginResult.Success>().phpSessionId.let {
                println("New PHP session ID is: $it")
                PhpSessionId(it)
            }
    }

}
