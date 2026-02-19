package seepick.localsportsclub.view

import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.DataStorageListener
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.NoopDataStorageListener
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.launchViewTask

class SyncerViewModel(
    private val dataStorage: DataStorage,
    private val fileResolver: FileResolver,
) : ViewModel(), DataStorageListener by NoopDataStorageListener {

    private val log = logger {}

    override fun onActivitiesAdded(activities: List<Activity>) {
        addOrRemoveActivities("Adding", activities) { venue, acts -> venue.addActivities(acts.toSet()) }
    }

    override fun onActivitiesDeleted(activities: List<Activity>) {
        addOrRemoveActivities("Deleting", activities) { venue, acts -> venue.removeActivities(acts.toSet()) }
    }

    private fun addOrRemoveActivities(
        logPrompt: String,
        activities: List<Activity>,
        addOrRemove: (Venue, List<Activity>) -> Unit,
    ) {
        launchViewTask("Unable to add/remove activities!", fileResolver) {
            log.debug { "$logPrompt ${activities.size} activities from/to their corresponding venues." }
            val venuesById = dataStorage.selectVisibleVenues().associateBy { it.id }
            activities.groupBy { it.venue.id }.forEach { (venueId, venueActivities) ->
                addOrRemove(venuesById[venueId]!!, venueActivities)
            }
        }
    }

    override fun onFreetrainingsAdded(freetrainings: List<Freetraining>) {
        removeOrAddFretraining("Adding", freetrainings) { venue, frees -> venue.addFreetrainings(frees) }
    }

    override fun onFreetrainingsDeleted(freetrainings: List<Freetraining>) {
        removeOrAddFretraining("Deleting", freetrainings) { venue, frees -> venue.removeFreetrainings(frees) }
    }

    private fun removeOrAddFretraining(
        logPrompt: String,
        freetrainings: List<Freetraining>,
        addOrRemove: (Venue, Set<Freetraining>) -> Unit,
    ) {
        launchViewTask("Unable to add/remove freetraining", fileResolver) {
            log.debug { "$logPrompt ${freetrainings.size} freetrainings from/to their corresponding venue." }
            val venuesById = dataStorage.selectVisibleVenues().associateBy { it.id }
            freetrainings.groupBy { it.venue.id }.forEach { (venueId, venueFreetraining) ->
                addOrRemove(venuesById[venueId]!!, venueFreetraining.toSet())
            }
        }
    }
}
