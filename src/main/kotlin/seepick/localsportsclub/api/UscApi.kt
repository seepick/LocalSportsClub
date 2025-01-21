package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url
import kotlinx.coroutines.delay
import seepick.localsportsclub.api.activity.ActivitiesFilter
import seepick.localsportsclub.api.activity.ActivitiesParser
import seepick.localsportsclub.api.activity.ActivityApi
import seepick.localsportsclub.api.activity.ActivityInfo
import seepick.localsportsclub.api.activity.FreetrainingInfo
import seepick.localsportsclub.api.activity.ServiceType
import seepick.localsportsclub.api.booking.BookingApi
import seepick.localsportsclub.api.booking.BookingResult
import seepick.localsportsclub.api.booking.CancelResult
import seepick.localsportsclub.api.checkin.CheckinApi
import seepick.localsportsclub.api.checkin.CheckinsPage
import seepick.localsportsclub.api.schedule.ScheduleApi
import seepick.localsportsclub.api.schedule.ScheduleRow
import seepick.localsportsclub.api.venue.VenueApi
import seepick.localsportsclub.api.venue.VenueDetails
import seepick.localsportsclub.api.venue.VenueInfo
import seepick.localsportsclub.api.venue.VenueParser
import seepick.localsportsclub.api.venue.VenuesFilter
import seepick.localsportsclub.service.model.Credentials

interface UscApi {
    suspend fun login(credentials: Credentials): LoginResult
    suspend fun fetchVenues(session: PhpSessionId, filter: VenuesFilter): List<VenueInfo>
    suspend fun fetchVenueDetail(session: PhpSessionId, slug: String): VenueDetails
    suspend fun fetchActivities(session: PhpSessionId, filter: ActivitiesFilter): List<ActivityInfo>
    suspend fun fetchFreetrainings(session: PhpSessionId, filter: ActivitiesFilter): List<FreetrainingInfo>
    suspend fun fetchScheduleRows(session: PhpSessionId): List<ScheduleRow>
    suspend fun fetchCheckinsPage(session: PhpSessionId, pageNr: Int): CheckinsPage
    suspend fun book(session: PhpSessionId, activityOrFreetrainingId: Int): BookingResult
    suspend fun cancel(session: PhpSessionId, activityOrFreetrainingId: Int): CancelResult
}

class MockUscApi : UscApi {
    private val log = logger {}

    override suspend fun login(credentials: Credentials): LoginResult =
        LoginResult.Failure("Nope, just a mock")

    override suspend fun fetchVenues(session: PhpSessionId, filter: VenuesFilter): List<VenueInfo> {
        log.debug { "Mock returning empty venues list." }
        delay(500)
        return emptyList()
    }

    override suspend fun fetchVenueDetail(session: PhpSessionId, slug: String): VenueDetails {
        delay(500)
        return VenueDetails(
            title = "My Title",
            slug = "my-title",
            description = "mock description",
            linkedVenueSlugs = emptyList(),
            websiteUrl = null,
            disciplines = listOf("Yoga"),
            importantInfo = "impo info",
            openingTimes = "open altijd",
            longitude = "1.0",
            latitude = "2.0",
            originalImageUrl = Url("http://mock/test.png"),
            postalCode = "1001AA",
            streetAddress = "Main Street 42",
            addressLocality = "Amsterdam, Netherlands"
        )
    }

    override suspend fun fetchActivities(session: PhpSessionId, filter: ActivitiesFilter): List<ActivityInfo> {
        log.debug { "Mock returning empty activities list." }
        delay(500)
        return emptyList()
    }

    override suspend fun fetchFreetrainings(session: PhpSessionId, filter: ActivitiesFilter): List<FreetrainingInfo> {
        log.debug { "Mock returning empty freetrainings list." }
        delay(500)
        return emptyList()
    }

    override suspend fun fetchScheduleRows(session: PhpSessionId): List<ScheduleRow> {
        log.debug { "Mock returning empty schedule list." }
        delay(500)
        return emptyList()
    }

    override suspend fun fetchCheckinsPage(session: PhpSessionId, pageNr: Int): CheckinsPage {
        log.debug { "Mock returning empty checkins page." }
        delay(500)
        return CheckinsPage.empty
    }

    override suspend fun book(session: PhpSessionId, activityOrFreetrainingId: Int): BookingResult {
        log.info { "Mock booking: $activityOrFreetrainingId" }
        delay(1_000)
        return BookingResult.BookingSuccess
//        return BookingResult.BookingFail("nope")
    }

    override suspend fun cancel(session: PhpSessionId, activityOrFreetrainingId: Int): CancelResult {
        log.info { "Mock cancel booking: $activityOrFreetrainingId" }
        delay(1_000)
        return CancelResult.CancelSuccess
    }
}

class UscApiAdapter(
    private val loginApi: LoginApi,
    private val venueApi: VenueApi,
    private val activityApi: ActivityApi,
    private val scheduleApi: ScheduleApi,
    private val checkinApi: CheckinApi,
    private val bookingApi: BookingApi,
) : UscApi {

    override suspend fun login(credentials: Credentials): LoginResult =
        loginApi.login(credentials)

    override suspend fun fetchVenues(session: PhpSessionId, filter: VenuesFilter) =
        venueApi.fetchPages(session, filter).flatMap {
            VenueParser.parseHtmlContent(it.content)
        }

    override suspend fun fetchVenueDetail(session: PhpSessionId, slug: String) =
        venueApi.fetchDetails(session, slug)

    override suspend fun fetchActivities(session: PhpSessionId, filter: ActivitiesFilter) =
        activityApi.fetchPages(session, filter, ServiceType.Courses).flatMap {
            ActivitiesParser.parseContent(it.content, filter.date)
        }

    override suspend fun fetchFreetrainings(session: PhpSessionId, filter: ActivitiesFilter): List<FreetrainingInfo> =
        activityApi.fetchPages(session, filter, ServiceType.FreeTraining).flatMap {
            ActivitiesParser.parseFreetrainingContent(it.content)
        }

    override suspend fun fetchScheduleRows(session: PhpSessionId) =
        scheduleApi.fetchScheduleRows(session)

    override suspend fun fetchCheckinsPage(session: PhpSessionId, pageNr: Int) =
        checkinApi.fetchPage(session, pageNr)

    override suspend fun book(session: PhpSessionId, activityOrFreetrainingId: Int): BookingResult =
        bookingApi.book(session, activityOrFreetrainingId)

    override suspend fun cancel(session: PhpSessionId, activityOrFreetrainingId: Int): CancelResult =
        bookingApi.cancel(session, activityOrFreetrainingId)
}
