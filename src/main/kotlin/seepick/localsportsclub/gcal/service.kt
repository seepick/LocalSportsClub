package seepick.localsportsclub.gcal

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.FileEntry
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.service.retry
import java.net.SocketException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone


interface GcalService {
    fun create(entry: GcalEntry)

    /** @return true if successfully deleted, false if not found/existing. */
    fun delete(deletion: GcalDeletion): Boolean
}

object NoopGcalService : GcalService {
    private val log = logger {}
    override fun create(entry: GcalEntry) {
        log.warn { "Not creating: $entry" }
    }

    override fun delete(deletion: GcalDeletion): Boolean {
        log.warn { "Not deleting: $deletion" }
        return true
    }
}

data class GcalDeletion(
    val day: LocalDate,
    val activityOrFreetrainingId: Int,
    val isActivity: Boolean,
)

sealed interface GcalEntry {
    val title: String
    val location: String
    val notes: String
    val isActivity: Boolean
    val activityOrFreetrainingId: Int

    data class GcalActivity(
        override val title: String,
        override val location: String,
        override val notes: String,
        val activityId: Int,
        val dateTimeRange: DateTimeRange,
    ) : GcalEntry {
        override val isActivity = true
        override val activityOrFreetrainingId = activityId
    }

    data class GcalFreetraining(
        override val title: String,
        override val location: String,
        override val notes: String,
        val freetrainingId: Int,
        val date: LocalDate,
    ) : GcalEntry {
        override val isActivity = false
        override val activityOrFreetrainingId = freetrainingId
    }
}

class RealGcalService(
    private val calendarId: String,
) : GcalService {
    private val log = logger {}
    private val applicationName = "LocalSportsClub"
    private val scopes = CalendarScopes.all()
    private val datastoreDir = FileResolver.resolve(DirectoryEntry.Gcal)
    private val credentialsJsonFile = FileResolver.resolve(FileEntry.GcalCredentials)
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val timeZone = TimeZone.getDefault().id
    private val zoneId = ZoneId.systemDefault()
    private val credentials: Credential by lazy {
        val clientSecrets = GoogleClientSecrets.load(jsonFactory, credentialsJsonFile.inputStream().reader())
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
        private fun GcalService.createDummy() {
            val start = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0).withNano(0)
            create(
                GcalEntry.GcalActivity(
                    activityId = 1337,
                    title = "from LSC",
                    dateTimeRange = DateTimeRange(
                        from = start,
                        to = start.plusHours(1),
                    ),
                    location = "location",
                    notes = "some notes",
                )
            )
        }

        private fun GcalService.deleteDummy() {
            delete(
                GcalDeletion(
                    day = LocalDate.now(),
                    activityOrFreetrainingId = 1337,
                    isActivity = true,
                )
            )
        }

        @JvmStatic
        fun main(args: Array<String>) {
            RealGcalService("bpdgd31mckn8niqcjpkju8197c@group.calendar.google.com")
//                .createDummy()
                .deleteDummy()
        }

        private const val PROP_IS_ACTIVITY = "isActivity"
        private const val PROP_ENTITY_ID = "activityOrFreetrainingId"
    }

    override fun create(entry: GcalEntry) {
        log.info { "Creating google calendar entry: $entry" }
        val created = retry(maxAttempts = 3, listOf(SocketException::class.java)) {
            calendar.events().insert(calendarId, entry.toEvent()).execute()
        }
        // created.id ... could store it in DB for later (direct) deletion on cancellation?!
        log.debug { "Successfully created entry at: ${created.htmlLink}" }
    }

    override fun delete(deletion: GcalDeletion): Boolean {
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
            log.info { "Not going to delete calendar event, not found..." }
            return false
        }
        retry(maxAttempts = 3, listOf(SocketException::class.java)) {
            calendar.events().delete(calendarId, found.id).execute()
        }
        log.debug { "Successfully deleted calendar event." }
        return true
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
