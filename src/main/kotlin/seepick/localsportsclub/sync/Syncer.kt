package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.ImageStorage
import seepick.localsportsclub.service.model.DummyDataGenerator
import seepick.localsportsclub.service.model.Venue

interface Syncer {
    suspend fun sync()
}

class SyncDispatcher {

    private val venueDboAddedListeners = mutableListOf<(VenueDbo) -> Unit>()
    private val venueAddedListeners = mutableListOf<(Venue) -> Unit>()
    private val activityDboAddedListeners = mutableListOf<(ActivityDbo) -> Unit>()

    fun registerVenueDboAdded(onVenueDboAdded: (VenueDbo) -> Unit) {
        venueDboAddedListeners += onVenueDboAdded
    }

    fun dispatchVenueDboAdded(venueDbo: VenueDbo) {
        venueDboAddedListeners.forEach {
            it(venueDbo)
        }
    }

    fun registerActivityDboAdded(onActivityDboAdded: (ActivityDbo) -> Unit) {
        activityDboAddedListeners += onActivityDboAdded
    }

    fun dispatchActivityDboAdded(activityDbo: ActivityDbo) {
        activityDboAddedListeners.forEach {
            it(activityDbo)
        }
    }

    fun registerVenueAdded(onVenueAdded: (Venue) -> Unit) {
        venueAddedListeners += onVenueAdded
    }

    suspend fun dispatchVenueAdded(venue: Venue) {
        withContext(Dispatchers.Main) {
            venueAddedListeners.forEach {
                it(venue)
            }
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
        venueSyncer.sync()
        activitiesSyncer.sync()
    }
}

class DelayedSyncer(
    private val venueRepo: VenueRepo,
    private val syncDispatcher: SyncDispatcher,
    private val imageStorage: ImageStorage,
) : Syncer {
    private val log = logger {}

    private val dispatchOnly = true

    override suspend fun sync() {
        log.info { "Delayed syncer delaying" }
        val bytes = withContext(Dispatchers.IO) {
            DelayedSyncer::class.java.getResourceAsStream("/defaultVenueImage.png")!!.readAllBytes()
        }
        DummyDataGenerator.randomVenueDbos(5, customSuffix = "sync").forEach { venueDbo ->
            delay(500)
            if (dispatchOnly) {
                syncDispatcher.dispatchVenueDboAdded(venueDbo)
            } else {
                val inserted = venueRepo.insert(venueDbo)
                imageStorage.saveVenueImage("${inserted.id}.png", bytes)
                syncDispatcher.dispatchVenueDboAdded(inserted)
            }
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
