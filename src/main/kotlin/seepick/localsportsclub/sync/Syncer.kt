package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.VenueDbo

interface Syncer {
    fun registerListener(listener: SyncerListener)
    suspend fun sync()
}

interface SyncerListener {
    fun onVenueDboAdded(venueDbo: VenueDbo)
    fun onActivityDbosAdded(activityDbos: List<ActivityDbo>)
    fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate)
    fun onFreetrainingDbosAdded(freetrainingDbos: List<FreetrainingDbo>)
    fun onFreetrainingDboUpdated(freetrainingDbo: FreetrainingDbo, field: FreetrainingFieldUpdate)
}

enum class ActivityFieldUpdate {
    IsBooked,
    WasCheckedin,
}

enum class FreetrainingFieldUpdate {
    WasCheckedin
}

object NoopSyncer : Syncer {
    private val log = logger {}

    override fun registerListener(listener: SyncerListener) {
    }

    override suspend fun sync() {
        log.info { "Noop syncer not doing anything." }
    }
}

class SyncerListenerDispatcher {
    private val listeners = mutableListOf<SyncerListener>()

    fun registerListener(listener: SyncerListener) {
        listeners += listener
    }

    fun dispatchOnVenueDboAdded(venueDbo: VenueDbo) {
        listeners.forEach {
            it.onVenueDboAdded(venueDbo)
        }
    }

    fun dispatchOnActivityDbosAdded(activityDbos: List<ActivityDbo>) {
        listeners.forEach {
            it.onActivityDbosAdded(activityDbos)
        }
    }

    fun dispatchOnActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        listeners.forEach {
            it.onActivityDboUpdated(activityDbo, field)
        }
    }

    fun dispatchOnFreetrainingDbosAdded(freetrainingDbos: List<FreetrainingDbo>) {
        listeners.forEach {
            it.onFreetrainingDbosAdded(freetrainingDbos)
        }
    }

    fun dispatchOnFreetrainingDboUpdated(freetrainingDbo: FreetrainingDbo, field: FreetrainingFieldUpdate) {
        listeners.forEach {
            it.onFreetrainingDboUpdated(freetrainingDbo, field)
        }
    }
}
