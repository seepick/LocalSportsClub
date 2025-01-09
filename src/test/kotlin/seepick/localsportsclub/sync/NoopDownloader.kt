package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url

object NoopDownloader : Downloader {
    private val log = logger {}

    override suspend fun downloadVenueImage(url: Url): ByteArray {
        log.info { "Noop downloadVenueImage." }
        return ByteArray(0)
    }
}
