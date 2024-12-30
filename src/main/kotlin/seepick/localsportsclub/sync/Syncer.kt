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
    fun onActivityDboAdded(activityDbo: ActivityDbo)
    fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate)
    fun onFreetrainingDboAdded(freetrainingDbo: FreetrainingDbo)
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

    fun dispatchOnActivityDboAdded(activityDbo: ActivityDbo) {
        listeners.forEach {
            it.onActivityDboAdded(activityDbo)
        }
    }

    fun dispatchOnActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        listeners.forEach {
            it.onActivityDboUpdated(activityDbo, field)
        }
    }

    fun dispatchOnFreetrainingDboAdded(freetrainingDbo: FreetrainingDbo) {
        listeners.forEach {
            it.onFreetrainingDboAdded(freetrainingDbo)
        }
    }

    fun dispatchOnFreetrainingDboUpdated(freetrainingDbo: FreetrainingDbo, field: FreetrainingFieldUpdate) {
        listeners.forEach {
            it.onFreetrainingDboUpdated(freetrainingDbo, field)
        }
    }
}
