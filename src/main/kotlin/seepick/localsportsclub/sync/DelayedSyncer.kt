package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.ImageStorage
import seepick.localsportsclub.service.model.DummyDataGenerator

class DelayedSyncer(
    private val venueRepo: VenueRepo,
    private val syncDispatcher: SyncDispatcher,
    private val imageStorage: ImageStorage,
) : Syncer {
    private val log = logger {}

    private val dispatchOnly = true

    override suspend fun sync() {
        log.info { "Delayed syncer delaying" }
        delay(2_000)
        log.info { "Delay sync done." }
    }

    private suspend fun generate() {
        val bytes = withContext(Dispatchers.IO) {
            DelayedSyncer::class.java.getResourceAsStream("/defaultVenueImage.png")!!.readAllBytes()
        }
        DummyDataGenerator.randomVenueDbos(5, customSuffix = "sync").forEach { venueDbo ->
            delay(500)
            if (dispatchOnly) {
                syncDispatcher.dispatchVenueDboAdded(venueDbo)
            } else {
                val inserted = venueRepo.insert(venueDbo)
                imageStorage.saveVenueImage("${inserted.slug}.png", bytes)
                syncDispatcher.dispatchVenueDboAdded(inserted)
            }
        }
    }
}
