package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.delay
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.service.model.ActivityState

interface Syncer {
    fun registerListener(listener: SyncerListener)
    suspend fun sync()
}

interface SyncerListener {
    fun alsoRegisterForBooking(): Boolean = true
    fun onVenueDbosAdded(venueDbos: List<VenueDbo>)
    fun onVenueDbosMarkedDeleted(venueDbos: List<VenueDbo>)

    fun onActivityDbosAdded(activityDbos: List<ActivityDbo>)
    fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate)
    fun onActivityDbosDeleted(activityDbos: List<ActivityDbo>)

    fun onFreetrainingDbosAdded(freetrainingDbos: List<FreetrainingDbo>)
    fun onFreetrainingDboUpdated(freetrainingDbo: FreetrainingDbo, field: FreetrainingFieldUpdate)
    fun onFreetrainingDbosDeleted(freetrainingDbos: List<FreetrainingDbo>)
}

sealed interface ActivityFieldUpdate {
    data object Teacher : ActivityFieldUpdate
    data class State(val oldState: ActivityState) : ActivityFieldUpdate
}

enum class FreetrainingFieldUpdate {
    State,
}

class NoopSyncer(
    private val dispatcher: SyncerListenerDispatcher,
    private val progress: SyncProgress,
) : Syncer {
    private val log = logger {}

    override fun registerListener(listener: SyncerListener) {
        dispatcher.registerListener(listener)
    }

    override suspend fun sync() {
        log.info { "Noop syncer not doing anything." }
        progress.start()
        try {
            delay(500)
        } finally {
            progress.stop()
        }
    }
}

class SyncerListenerDispatcher {
    private val log = logger {}
    private val listeners = mutableListOf<SyncerListener>()

    fun registerListener(listener: SyncerListener) {
        log.debug { "registering SyncerListener: ${listener::class.qualifiedName}" }
        listeners += listener
    }

    fun dispatchOnVenueDbosAdded(venueDbos: List<VenueDbo>) {
        if (venueDbos.isNotEmpty()) {
            listeners.forEach {
                it.onVenueDbosAdded(venueDbos)
            }
        }
    }

    fun dispatchOnVenueDbosMarkedDeleted(venueDbos: List<VenueDbo>) {
        if (venueDbos.isNotEmpty()) {
            listeners.forEach {
                it.onVenueDbosMarkedDeleted(venueDbos)
            }
        }
    }

    fun dispatchOnActivityDbosAdded(activityDbos: List<ActivityDbo>) {
        if (activityDbos.isNotEmpty()) {
            listeners.forEach {
                it.onActivityDbosAdded(activityDbos)
            }
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

    fun dispatchOnActivityDbosDeleted(activityDbos: List<ActivityDbo>) {
        if (activityDbos.isNotEmpty()) {
            listeners.forEach {
                it.onActivityDbosDeleted(activityDbos)
            }
        }
    }

    fun dispatchOnFreetrainingDbosDeleted(freetrainingDbos: List<FreetrainingDbo>) {
        if (freetrainingDbos.isNotEmpty()) {
            listeners.forEach {
                it.onFreetrainingDbosDeleted(freetrainingDbos)
            }
        }
    }
}
