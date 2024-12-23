package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.service.model.Venue

interface Syncer {
    suspend fun sync()
}

class SyncDispatcher {

    private val venueDboAddedListeners = mutableListOf<(VenueDbo) -> Unit>()
    private val venueAddedListeners = mutableListOf<(Venue) -> Unit>()
    private val activityDboAddedListeners = mutableListOf<(ActivityDbo) -> Unit>()

    fun registerVenueDboAdded(onVenueDboAdded: (VenueDbo) -> Unit) {
        venueDboAddedListeners += onVenueDboAdded
    }

    fun dispatchVenueDboAdded(venueDbo: VenueDbo) {
        venueDboAddedListeners.forEach {
            it(venueDbo)
        }
    }

    fun registerActivityDboAdded(onActivityDboAdded: (ActivityDbo) -> Unit) {
        activityDboAddedListeners += onActivityDboAdded
    }

    fun dispatchActivityDboAdded(activityDbo: ActivityDbo) {
        activityDboAddedListeners.forEach {
            it(activityDbo)
        }
    }

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
    override suspend fun sync() {
        log.info { "Noop syncer not doing anything." }
    }
}
