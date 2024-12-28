package seepick.localsportsclub.view.activity

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import seepick.localsportsclub.service.SortingDelegate
import seepick.localsportsclub.service.findIndexFor
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.DataStorageListener
import seepick.localsportsclub.service.model.NoopDataStorageListener
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.search.ActivitySearch
import seepick.localsportsclub.view.venue.VenueEditModel

class ActivityViewModel(
    private val dataStorage: DataStorage,
) : ViewModel(), DataStorageListener by NoopDataStorageListener {

    private val log = KotlinLogging.logger {}

    private val _allActivities = mutableStateListOf<Activity>()
    val allActivities: List<Activity> = _allActivities
    private val _activities = mutableStateListOf<Activity>()
    val activities: List<Activity> = _activities

    private val _selectedActivity = MutableStateFlow<Activity?>(null)
    val selectedActivity = _selectedActivity.asStateFlow()

    val selectedVenue: StateFlow<Venue?> = selectedActivity.map {
        println("activity changed")
        it?.venue?.let {
            dataStorage.selectVenueById(it.id)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = null
    )

    val searching = ActivitySearch(::resetActivities)
    val sorting = SortingDelegate(activtiesTableColumns, resetSort = ::resetActivities)
    val venueEdit = VenueEditModel()

    fun onStartUp() {
        log.info { "On startup: Filling initial data." }
        _allActivities.addAll(dataStorage.selectAllActivities())
        resetActivities()
    }

    override fun onActivityAdded(activity: Activity) {
        _allActivities.add(activity)
        if (searching.matches(activity)) {
            val index = findIndexFor(_activities, activity, sorting.selectedColumnValueExtractor)
            _activities.add(index, activity)
        }
    }

    fun onActivityClicked(activity: Activity) {
        log.trace { "Selected: $activity" }
        viewModelScope.launch {
            _selectedActivity.value = activity
//        _selectedActivity.update { activity }
        }
    }

    fun updateVenue() {
        selectedVenue.value!!.also { venue ->
            venueEdit.updatePropertiesOf(venue)
            dataStorage.update(venue)
        }
    }

    private fun resetActivities() {
        _activities.clear()
        _activities.addAll(_allActivities.filter { searching.matches(it) }.let {
            sorting.sortIt(it)
        })
    }
}
