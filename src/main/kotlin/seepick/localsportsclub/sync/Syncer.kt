package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking

interface Syncer {
    fun sync()
}

class RealSyncerAdapter(
    private val venueSyncer: VenueSyncer,
) : Syncer {
    private val log = logger {}
    override fun sync() {
        log.debug { "Syncing ..." }
        runBlocking {
            venueSyncer.sync()
        }
    }

}

