package seepick.localsportsclub.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.launch
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.DataStorageListener
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.NoopDataStorageListener

class SyncerViewModel(
    private val dataStorage: DataStorage,
) : ViewModel(), DataStorageListener by NoopDataStorageListener {

    private val log = logger {}

    override fun onActivitiesAdded(activities: List<Activity>) {
        addOrRemoveActivities("Adding", activities) { venues, add -> venues += add }
    }

    override fun onActivitiesDeleted(activities: List<Activity>) {
        addOrRemoveActivities("Deleting", activities) { venues, add -> venues -= add }
    }

    private fun addOrRemoveActivities(
        logPrompt: String, activities: List<Activity>, addOrRemove: (MutableList<Activity>, Set<Activity>) -> Unit
    ) {
        viewModelScope.launch {
            log.debug { "$logPrompt ${activities.size} activities from/to their corresponding venues." }
            val venuesById = dataStorage.selectVisibleVenues().associateBy { it.id }
            activities.groupBy { it.venue.id }.forEach { (venueId, venueActivities) ->
                addOrRemove(venuesById[venueId]!!.activities, venueActivities.toSet())
            }
        }
    }

    override fun onFreetrainingsAdded(freetrainings: List<Freetraining>) {
        removeOrAddFretraining("Adding", freetrainings) { venues, add -> venues += add }
    }

    override fun onFreetrainingsDeleted(freetrainings: List<Freetraining>) {
        removeOrAddFretraining("Deleting", freetrainings) { venues, delete -> venues -= delete }
    }

    private fun removeOrAddFretraining(
        logPrompt: String,
        freetrainings: List<Freetraining>,
        addOrRemove: (MutableList<Freetraining>, Set<Freetraining>) -> Unit
    ) {
        viewModelScope.launch {
            log.debug { "$logPrompt ${freetrainings.size} freetrainings from/to their corresponding venue." }
            val venuesById = dataStorage.selectVisibleVenues().associateBy { it.id }
            freetrainings.groupBy { it.venue.id }.forEach { (venueId, venueFreetraining) ->
                addOrRemove(venuesById[venueId]!!.freetrainings, venueFreetraining.toSet())
            }
        }
    }
}
