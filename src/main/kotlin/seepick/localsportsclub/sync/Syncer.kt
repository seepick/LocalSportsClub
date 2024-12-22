package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import seepick.localsportsclub.persistence.VenuesRepo
import seepick.localsportsclub.service.ImageStorage
import seepick.localsportsclub.service.model.DummyDataGenerator
import seepick.localsportsclub.service.model.Venue

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
        newSuspendedTransaction(Dispatchers.IO) {
            venueSyncer.sync()
        }
    }
}

class DelayedSyncer(
    private val venuesRepo: VenuesRepo,
    private val syncDispatcher: SyncDispatcher,
    private val imageStorage: ImageStorage,
) : Syncer {
    private val log = logger {}

    override suspend fun sync() {
        log.info { "Delayed syncer delaying" }
//        val bytes = withContext(Dispatchers.IO) {
//            DelayedSyncer::class.java.getResourceAsStream("/defaultVenueImage.png")!!.readAllBytes()
//        }
        DummyDataGenerator.randomVenues(5, customSuffix = "sync").forEach { venue ->
            delay(500)
//            val dbo = venuesRepo.insert(venue.toDbo())
//            imageStorage.saveVenue(dbo.id, bytes, "png")
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
