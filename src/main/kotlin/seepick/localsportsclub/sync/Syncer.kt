package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.ImageStorage
import seepick.localsportsclub.service.model.DummyDataGenerator
import seepick.localsportsclub.service.model.Venue

interface Syncer {
    suspend fun sync()
}

class SyncDispatcher {

    private val venueAddedListeners = mutableListOf<(Venue) -> Unit>()
    private val venueUpdatedListeners = mutableListOf<(Venue) -> Unit>()
    private val activityAddedListeners = mutableListOf<(ActivityDbo) -> Unit>()

    fun registerVenueAdded(onVenueAdded: (Venue) -> Unit) {
        venueAddedListeners += onVenueAdded
    }

    fun dispatchVenueAdded(venue: Venue) {
        venueAddedListeners.forEach {
            it(venue)
        }
    }

    fun registerVenueUpdated(onVenueUpdated: (Venue) -> Unit) {
        venueUpdatedListeners += onVenueUpdated
    }

    fun dispatchVenueUpdated(venue: Venue) {
        venueUpdatedListeners.forEach {
            it(venue)
        }
    }

    // FIXME data storage should register, not the UI directly! (same for venue)
    fun registerActivityAdded(onActivityAdded: (ActivityDbo) -> Unit) {
        activityAddedListeners += onActivityAdded
    }

    fun dispatchActivityDboAdded(activity: ActivityDbo) {
        activityAddedListeners.forEach {
            it(activity)
        }
    }
}

class RealSyncerAdapter(
    private val venueSyncer: VenueSyncer,
    private val activitiesSyncer: ActivitiesSyncer,
) : Syncer {
    private val log = logger {}
    override suspend fun sync() {
        log.debug { "Syncing ..." }
        newSuspendedTransaction(Dispatchers.IO) {
            venueSyncer.sync()
            activitiesSyncer.sync()
        }
    }
}

class DelayedSyncer(
    private val venueRepo: VenueRepo,
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
