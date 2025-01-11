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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone


interface GcalService {
    fun create(entry: GcalEntry)
}

object NoopGcalService : GcalService {
    private val log = logger {}
    override fun create(entry: GcalEntry) {
        log.warn { "Not creating: $entry" }
    }
}

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
            .setDataStoreFactory(FileDataStoreFactory(datastoreDir))
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }
    private val calendar: Calendar by lazy {
        log.info { "Connecting to google calendar..." }
        Calendar.Builder(httpTransport, jsonFactory, credentials).setApplicationName(applicationName).build()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val start = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0).withNano(0)
            RealGcalService("bpdgd31mckn8niqcjpkju8197c@group.calendar.google.com").create(
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
    }

    override fun create(entry: GcalEntry) {
        log.info { "Creating google calendar entry: $entry" }
        val created = calendar.events().insert(calendarId, entry.toEvent()).execute()
        // created.id ... could store it in DB for later deletion on cancellation?!
        log.debug { "Successfully created entry at: ${created.htmlLink}" }
    }

    private fun GcalEntry.toEvent() =
        Event()
            .setSummary("☑️ $title")
            .setLocation(location)
            .setDescription(notes)
            .also { date(this, it) }
            .setExtendedProperties(Event.ExtendedProperties().also {
                it.set("isActivity", isActivity)
                it.set("activityOrFreetrainingId", activityOrFreetrainingId)
            })

    private fun date(entry: GcalEntry, event: Event) {
        when (entry) {
            is GcalEntry.GcalActivity -> {
                event.setStart(
                    EventDateTime().setDateTime(entry.dateTimeRange.from.toGoogleDateTime()).setTimeZone(timeZone)
                )
                event.setEnd(
                    EventDateTime().setDateTime(entry.dateTimeRange.to.toGoogleDateTime()).setTimeZone(timeZone)
                )
            }

            is GcalEntry.GcalFreetraining -> {
                val start = DateTime(dateFormatter.format(entry.date))
                val end = DateTime(dateFormatter.format(entry.date.plusDays(1)))
                event.setStart(EventDateTime().setDate(start).setTimeZone(timeZone))
//                        it.setEndTimeUnspecified(true)
                event.setEnd(EventDateTime().setDate(end).setTimeZone(timeZone))
            }
        }
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

    private fun LocalDateTime.toGoogleDateTime() =
        DateTime(atZone(zoneId).toEpochSecond() * 1_000)
}
