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
    fun onVenueDbosAdded(addedVenues: List<VenueDbo>)
    fun onVenueDbosMarkedDeleted(deletedVenues: List<VenueDbo>)
    fun onVenueDbosMarkedUndeleted(undeletedVenues: List<VenueDbo>)

    fun onActivityDbosAdded(addedActivities: List<ActivityDbo>)
    fun onActivityDboUpdated(updatedActivity: ActivityDbo, field: ActivityFieldUpdate)
    fun onActivityDbosDeleted(deletedActivities: List<ActivityDbo>)

    fun onFreetrainingDbosAdded(addedFreetrainings: List<FreetrainingDbo>)
    fun onFreetrainingDboUpdated(updatedFreetraining: FreetrainingDbo, field: FreetrainingFieldUpdate)
    fun onFreetrainingDbosDeleted(deletedFreetrainings: List<FreetrainingDbo>)
}

sealed interface ActivityFieldUpdate {
    data object Teacher : ActivityFieldUpdate
    data object Description : ActivityFieldUpdate
    data object SpotsLeft : ActivityFieldUpdate
    data object CancellationLimit : ActivityFieldUpdate
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
            progress.stop(isError = false)
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

    fun dispatchOnVenueDbosAdded(addedVenues: List<VenueDbo>) {
        if (addedVenues.isNotEmpty()) {
            listeners.forEach {
                it.onVenueDbosAdded(addedVenues)
            }
        }
    }

    fun dispatchOnVenueDbosMarkedDeleted(deletedVenues: List<VenueDbo>) {
        if (deletedVenues.isNotEmpty()) {
            listeners.forEach {
                it.onVenueDbosMarkedDeleted(deletedVenues)
            }
        }
    }

    fun dispatchOnVenueDbosMarkedUndeleted(undeletedVenues: List<VenueDbo>) {
        if (undeletedVenues.isNotEmpty()) {
            listeners.forEach {
                it.onVenueDbosMarkedUndeleted(undeletedVenues)
            }
        }
    }

    fun dispatchOnActivityDbosAdded(addedActivities: List<ActivityDbo>) {
        if (addedActivities.isNotEmpty()) {
            listeners.forEach {
                it.onActivityDbosAdded(addedActivities)
            }
        }
    }

    fun dispatchOnActivityDboUpdated(updatedActivity: ActivityDbo, field: ActivityFieldUpdate) {
        listeners.forEach {
            it.onActivityDboUpdated(updatedActivity, field)
        }
    }

    fun dispatchOnFreetrainingDbosAdded(addedFreetrainings: List<FreetrainingDbo>) {
        listeners.forEach {
            it.onFreetrainingDbosAdded(addedFreetrainings)
        }
    }

    fun dispatchOnFreetrainingDboUpdated(updatedFreetraining: FreetrainingDbo, field: FreetrainingFieldUpdate) {
        listeners.forEach {
            it.onFreetrainingDboUpdated(updatedFreetraining, field)
        }
    }

    fun dispatchOnActivityDbosDeleted(deletedActivities: List<ActivityDbo>) {
        if (deletedActivities.isNotEmpty()) {
            listeners.forEach {
                it.onActivityDbosDeleted(deletedActivities)
            }
        }
    }

    fun dispatchOnFreetrainingDbosDeleted(deletedFreetrainings: List<FreetrainingDbo>) {
        if (deletedFreetrainings.isNotEmpty()) {
            listeners.forEach {
                it.onFreetrainingDbosDeleted(deletedFreetrainings)
            }
        }
    }
}
