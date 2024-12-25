package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.delay
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.ImageStorage

class DelayedSyncer(
    private val venueRepo: VenueRepo,
    private val imageStorage: ImageStorage,
    private val dispatcher: SyncerListenerDispatcher,
) : Syncer {
    private val log = logger {}

    override suspend fun sync() {
        log.info { "Delayed syncer delaying" }
        delay(2_000)
        // generate()
        log.info { "Delay sync done." }
    }

    override fun registerListener(listener: SyncerListener) {
        dispatcher.registerListener(listener)
    }

}
