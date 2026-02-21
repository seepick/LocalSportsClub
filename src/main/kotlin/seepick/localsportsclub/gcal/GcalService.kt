package seepick.localsportsclub.gcal

import io.github.oshai.kotlinlogging.KotlinLogging.logger

interface GcalService {
    fun create(calendarId: String, entry: GcalEntry)
    fun delete(calendarId: String, deletion: GcalDeletion)
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
