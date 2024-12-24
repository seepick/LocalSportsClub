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
    private val imageStorage: ImageStorage,
    private val dispatcher: SyncerListenerDispatcher,
) : Syncer {
    private val log = logger {}

    private val dispatchOnly = true

    override suspend fun sync() {
        log.info { "Delayed syncer delaying" }
        delay(2_000)
        // generate()
        log.info { "Delay sync done." }
    }

    override fun registerListener(listener: SyncerListener) {
        dispatcher.registerListener(listener)
    }

    private suspend fun generate() {
        val bytes = withContext(Dispatchers.IO) {
            DelayedSyncer::class.java.getResourceAsStream("/defaultVenueImage.png")!!.readAllBytes()
        }
        DummyDataGenerator.randomVenueDbos(5, customSuffix = "sync").forEach { venueDbo ->
            delay(500)
            if (dispatchOnly) {
                dispatcher.dispatchOnVenueDboAdded(venueDbo)
            } else {
                val inserted = venueRepo.insert(venueDbo)
                imageStorage.saveVenueImage("${inserted.slug}.png", bytes)
                dispatcher.dispatchOnVenueDboAdded(inserted)
            }
        }
    }
}
