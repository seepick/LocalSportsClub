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
        viewModelScope.launch {
            log.debug { "Linking ${activities.size} activities into their corresponding venues." }
            val venuesById = dataStorage.selectAllVenues().associateBy { it.id }
            activities.groupBy { it.venue.id }.forEach { (venueId, venueActivities) ->
                venuesById[venueId]!!.activities += venueActivities
            }
        }
    }

    override fun onFreetrainingsAdded(freetrainings: List<Freetraining>) {
        viewModelScope.launch {
            log.debug { "Linking ${freetrainings.size} freetrainings into their corresponding venues." }
            val venuesById = dataStorage.selectAllVenues().associateBy { it.id }
            freetrainings.groupBy { it.venue.id }.forEach { (venueId, venueFreetraining) ->
                venuesById[venueId]!!.freetrainings += venueFreetraining
            }
        }
    }
}
