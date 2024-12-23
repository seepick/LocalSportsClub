package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging

class RealSyncerAdapter(
    private val venueSyncer: VenueSyncer,
    private val activitiesSyncer: ActivitiesSyncer,
) : Syncer {
    private val log = KotlinLogging.logger {}
    override suspend fun sync() {
        log.debug { "Syncing ..." }
        venueSyncer.sync()
        activitiesSyncer.sync()
    }
}
