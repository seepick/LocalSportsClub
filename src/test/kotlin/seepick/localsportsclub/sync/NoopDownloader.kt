package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.net.URL

object NoopDownloader : Downloader {
    private val log = logger {}

    override suspend fun downloadVenueImage(url: URL): ByteArray {
        log.info { "Noop downloadVenueImage." }
        return ByteArray(0)
    }
}
