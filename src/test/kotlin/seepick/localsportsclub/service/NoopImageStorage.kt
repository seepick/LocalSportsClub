package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger

object NoopImageStorage : ImageStorage {
    private val log = logger {}
    override fun saveAndResizeVenueImage(fileName: String, bytes: ByteArray) {
        log.debug { "Noop not saving venue image: $fileName" }
    }
}
