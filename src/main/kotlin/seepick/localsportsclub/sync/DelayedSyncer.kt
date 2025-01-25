package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.delay

class DelayedSyncer(
    private val dispatcher: SyncerListenerDispatcher,
) : Syncer {
    private val log = logger {}

    override suspend fun sync() {
        log.info { "Delayed syncer delaying" }
        delay(2_000)
        log.info { "Delay sync done." }
    }

    override fun registerListener(listener: SyncerListener) {
        dispatcher.registerListener(listener)
    }
}
