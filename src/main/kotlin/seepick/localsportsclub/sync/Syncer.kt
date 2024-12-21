package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.delay
import seepick.localsportsclub.api.domain.Venue
import seepick.localsportsclub.persistence.VenuesRepo
import seepick.localsportsclub.service.DummyDataGenerator

interface Syncer {
    suspend fun sync()
}

class SyncDispatcher {
    private val venueAddedListeners = mutableListOf<(Venue) -> Unit>()
    private val venueUpdatedListeners = mutableListOf<(Venue) -> Unit>()

    fun registerVenueAdded(onVenueAdded: (Venue) -> Unit) {
        venueAddedListeners += onVenueAdded
    }

    fun registerVenueUpdated(onVenueUpdated: (Venue) -> Unit) {
        venueUpdatedListeners += onVenueUpdated
    }

    fun dispatchVenueAdded(venue: Venue) {
        venueAddedListeners.forEach {
            it(venue)
        }
    }

    fun dispatchVenueUpdated(venue: Venue) {
        venueUpdatedListeners.forEach {
            it(venue)
        }
    }
}

class RealSyncerAdapter(
    private val venueSyncer: VenueSyncer,
) : Syncer {
    private val log = logger {}
    override suspend fun sync() {
        log.debug { "Syncing ..." }
        venueSyncer.sync()
    }
}

class DelayedSyncer(
    private val venuesRepo: VenuesRepo,
    private val syncDispatcher: SyncDispatcher,
) : Syncer {
    private val log = logger {}

    override suspend fun sync() {
        log.info { "Delayed syncer delaying" }
        DummyDataGenerator.generateVenues(5, customSuffix = "sync").forEachIndexed { index, venue ->
            delay(500)
//            venuesRepo.insert(venue.toDbo())
            syncDispatcher.dispatchVenueAdded(venue)
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
