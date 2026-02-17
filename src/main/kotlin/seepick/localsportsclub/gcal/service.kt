package seepick.localsportsclub.gcal

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.service.retry
import seepick.localsportsclub.service.singles.SinglesService
import java.net.SocketException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

fun SinglesService.readCalendarIdOrThrow() =
    preferences.gcal.maybeCalendarId ?: error("No calendar ID set!")

interface GcalService {
    fun create(calendarId: String, entry: GcalEntry)
    fun delete(calendarId: String, deletion: GcalDeletion)
}

class PrefsEnabledGcalService(
    private val singlesService: SinglesService,
) : GcalService {

    private val delegate by lazy {
        val calendarId = singlesService.preferences.gcal.maybeCalendarId
        if (calendarId == null) NoopGcalService
        else RealGcalService()
    }

    override fun create(calendarId: String, entry: GcalEntry) {
        delegate.create(calendarId, entry)
    }

    override fun delete(calendarId: String, deletion: GcalDeletion) {
        delegate.delete(calendarId, deletion)
    }
}

object NoopGcalService : GcalService {
    private val log = logger {}
    override fun create(calendarId: String, entry: GcalEntry) {
        log.debug { "Not creating: $entry" }
    }

    override fun delete(calendarId: String, deletion: GcalDeletion) {
        log.debug { "Not deleting: $deletion" }
    }
}


class RealGcalService : GcalService {
    private val log = logger {}
    private val applicationName = "LocalSportsClub"
    private val scopes = setOf(CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_EVENTS)
    private val datastoreDir = FileResolver.resolve(DirectoryEntry.Gcal)
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val timeZone = TimeZone.getDefault().id
    private val zoneId = ZoneId.systemDefault()

    private val credentials: Credential by lazy {
        val clientSecrets = GoogleClientSecrets.load(jsonFactory, GcalCredentialsLoader.buildReader())
        val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, scopes)
            .setDataStoreFactory(FileDataStoreFactory(datastoreDir)).setAccessType("offline").build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    private val calendar: Calendar by lazy {
        log.info { "Connecting to google calendar..." }
        Calendar.Builder(httpTransport, jsonFactory, credentials).setApplicationName(applicationName).build()
    }

    companion object {

        private const val PROP_IS_ACTIVITY = "isActivity"
        private const val PROP_ENTITY_ID = "activityOrFreetrainingId"
    }

    fun testConnection(calendarId: String): GcalConnectionTest {
        log.debug { "Testing connection to calendar: $calendarId" }
        return try {
            calendar.calendars().get(calendarId).execute()
            log.debug { "GCal connection test succeeded." }
            GcalConnectionTest.Success
        } catch (e: GoogleJsonResponseException) {
            if (e.details.code == 404) {
                log.debug { "GCal connection test failed: ${e.details}" }
                GcalConnectionTest.Fail("Calendar not found.")
            } else {
                throw e
            }
        }
    }
    // if TokenResponseException thrown (bad token) => delete stored credentials file

    override fun create(calendarId: String, entry: GcalEntry) {
        log.info { "Creating google calendar entry: $entry" }
        val created = retry(maxAttempts = 3, listOf(SocketException::class.java)) {
            calendar.events().insert(calendarId, entry.toEvent()).execute()
        }
        // created.id ... could store it in DB for later (direct) deletion on cancellation?!
        log.debug { "Successfully created entry at: ${created.htmlLink}" }
    }

    override fun delete(calendarId: String, deletion: GcalDeletion) {
        log.info { "Deleting: $deletion" }
        val events = calendar.events().list(calendarId)
            .setSingleEvents(true)
            .setTimeMin(LocalDateTime.of(deletion.day, LocalTime.of(0, 0)).toGoogleDateTime())
            .setTimeMax(LocalDateTime.of(deletion.day, LocalTime.of(23, 59)).toGoogleDateTime())
            .execute().items
        val found = events.firstOrNull { e ->
            e.getPrivateExtendedProperty(PROP_IS_ACTIVITY)?.toBoolean() == deletion.isActivity &&
                    e.getPrivateExtendedProperty(PROP_ENTITY_ID)?.toInt() == deletion.activityOrFreetrainingId
        }
        if (found == null) {
            log.warn { "Not going to delete calendar event, not found: $deletion" }
            return
        }
        retry(maxAttempts = 3, listOf(SocketException::class.java)) {
            calendar.events().delete(calendarId, found.id).execute()
        }
        log.debug { "Successfully deleted calendar event." }
    }

    private fun Event.getPrivateExtendedProperty(key: String): String? =
        extendedProperties?.private?.get(key)

    private fun Event.setPrivateExtendedProperties(properties: Map<String, String>) = apply {
        extendedProperties = Event.ExtendedProperties().apply {
            private = properties
        }
    }

    private fun GcalEntry.toEvent() =
        Event().setSummary("☑️ $title")
            .setLocation(location)
            .setDescription(notes)
            .also { setDateForEntry(this, it) }
            .setPrivateExtendedProperties(
                mapOf(
                    PROP_IS_ACTIVITY to isActivity.toString(),
                    PROP_ENTITY_ID to activityOrFreetrainingId.toString(),
                )
            )

    private fun setDateForEntry(entry: GcalEntry, event: Event) {
        when (entry) {
            is GcalEntry.GcalActivity -> {
                event.setStartEnd(entry.dateTimeRange)
            }

            is GcalEntry.GcalFreetraining -> {
                event.setStartEnd(entry.date)
            }
        }
    }

    private fun Event.setStartEnd(date: LocalDate) {
        setStart(date.toEventDateTime())
        setEnd(date.plusDays(1).toEventDateTime())
    }

    private fun Event.setStartEnd(dateTimeRange: DateTimeRange) {
        setStart(dateTimeRange.from.toEventDateTime())
        setEnd(dateTimeRange.to.toEventDateTime())
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

    private fun LocalDate.toEventDateTime() = EventDateTime().setDate(toGoogleDateTime()).setTimeZone(timeZone)

    private fun LocalDate.toGoogleDateTime() = DateTime(dateFormatter.format(this))

    private fun LocalDateTime.toEventDateTime() =
        EventDateTime().setDateTime(this.toGoogleDateTime()).setTimeZone(timeZone)

    private fun LocalDateTime.toGoogleDateTime() = DateTime(atZone(zoneId).toEpochSecond() * 1_000)

}

sealed interface GcalConnectionTest {
    data object Success : GcalConnectionTest
    data class Fail(val message: String) : GcalConnectionTest
}
