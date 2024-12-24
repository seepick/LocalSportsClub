package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.service.model.Venue

interface Syncer {
    fun registerListener(listener: SyncerListener)
    suspend fun sync()
}

interface SyncerListener {
    fun onVenueDboAdded(venueDbo: VenueDbo)
    fun onActivityDboAdded(activityDbo: ActivityDbo)
    fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate)
}

enum class ActivityFieldUpdate {
    Scheduled
}

class SyncDispatcherX {

    private val venueAddedListeners = mutableListOf<(Venue) -> Unit>()

    fun registerVenueAdded(onVenueAdded: (Venue) -> Unit) {
        venueAddedListeners += onVenueAdded
    }

    fun dispatchVenueAdded(venue: Venue) {
//        withContext(Dispatchers.Main) { // TODO switch back to UI pool?!
        venueAddedListeners.forEach {
            it(venue)
        }
//        }
    }
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
}
