package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction

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
        transaction {
            runBlocking {
//                venueSyncer.sync()
//                activitiesSyncer.sync()
//                freetrainingSyncer.sync()
//                scheduleSyncer.sync()
                checkinSyncer.sync()
                cleanupSyncer.sync()
            }
        }
    }
}
