package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger

class SyncerFacade(
    private val venueSyncer: VenueSyncer,
    private val activitiesSyncer: ActivitiesSyncer,
    private val freetrainingSyncer: FreetrainingSyncer,
    private val scheduleSyncer: ScheduleSyncer,
    private val checkinSyncer: CheckinSyncer,
    private val cleanupSyncer: CleanupSyncer,
    private val dispatcher: SyncerListenerDispatcher,
) : Syncer {
    private val log = logger {}

    override fun registerListener(listener: SyncerListener) {
        dispatcher.registerListener(listener)
    }

    override suspend fun sync() {
        log.debug { "Syncing ..." }
        venueSyncer.sync()
        activitiesSyncer.sync()
        freetrainingSyncer.sync()
        scheduleSyncer.sync()
        checkinSyncer.sync()
        cleanupSyncer.sync()
    }
}
