package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging

class SyncerFacade(
    private val venueSyncer: VenueSyncer,
    private val activitiesSyncer: ActivitiesSyncer,
    private val scheduleSyncer: ScheduleSyncer,
) : Syncer {
    private val log = KotlinLogging.logger {}
    override suspend fun sync() {
        log.debug { "Syncing ..." }
        venueSyncer.sync()
        activitiesSyncer.sync()
        scheduleSyncer.sync()
    }
}
