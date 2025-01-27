package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.delay
import seepick.localsportsclub.service.DummyGenerator

class DelayedSyncer(
    private val dispatcher: SyncerListenerDispatcher,
    private val progress: SyncProgress,
) : Syncer {
    private val log = logger {}

    override suspend fun sync() {
        log.info { "Delayed syncer delaying" }
        progress.start()
        progress.onProgress("Venues")
        delay(1_000)
        dispatcher.dispatchOnVenueDbosAdded(listOf(DummyGenerator.venue()))
        delay(1_000)

        progress.onProgress("Activities")
        delay(500)
        progress.onProgress("Activities", "Day 1/3")
        delay(2_000)
        progress.onProgress("Activities", "Day 2/3")
        delay(2_000)
        progress.onProgress("Activities", "Day 3/3")
        delay(2_000)
        progress.onProgress("Checkins", "")

        log.info { "Delay sync done." }
        progress.stop()
    }

    override fun registerListener(listener: SyncerListener) {
        dispatcher.registerListener(listener)
    }
}
