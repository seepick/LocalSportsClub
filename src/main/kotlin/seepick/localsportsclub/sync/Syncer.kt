package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.delay
import seepick.localsportsclub.api.domain.Venue
import seepick.localsportsclub.service.DummyDataGenerator

interface Syncer {
    suspend fun sync()
}

class SyncDispatcher {
    private val venueAddedListeners = mutableListOf<(Venue) -> Unit>()
    fun registerOnVenueAdded(onVenueAdded: (Venue) -> Unit) {
        venueAddedListeners += onVenueAdded
    }

    fun dispatchOnVenueAdded(venue: Venue) {
        venueAddedListeners.forEach {
            it(venue)
        }
    }
}

class RealSyncerAdapter(
    private val syncDispatcher: SyncDispatcher,
    private val venueSyncer: VenueSyncer,
) : Syncer {
    private val log = logger {}
    override suspend fun sync() {
        log.debug { "Syncing ..." }
        venueSyncer.sync()
    }
}

class DelayedSyncer(
    private val syncDispatcher: SyncDispatcher,
) : Syncer {
    private val log = logger {}
    override suspend fun sync() {
        log.info { "Delayed syncer delaying" }
        DummyDataGenerator.generateVenues(10, customSuffix = "sync").forEach { venue ->
            delay(500)
            syncDispatcher.dispatchOnVenueAdded(venue)
        }
        log.info { "Delay sync done." }
    }

}

object NoopSyncer : Syncer {
    private val log = logger {}
    override suspend fun sync() {
        log.info { "Noop syncer not doing anything." }
    }
}
