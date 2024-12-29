package seepick.localsportsclub.view.activity

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import seepick.localsportsclub.service.Clock
import seepick.localsportsclub.service.SortingDelegate
import seepick.localsportsclub.service.findIndexFor
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.DataStorageListener
import seepick.localsportsclub.service.model.NoopDataStorageListener
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.prettyPrint
import seepick.localsportsclub.service.search.ActivitySearch
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.table.CellRenderer.TextRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted
import seepick.localsportsclub.view.venue.detail.VenueEditModel

class ActivityViewModel(
    private val dataStorage: DataStorage,
    private val clock: Clock,
) : ViewModel(), DataStorageListener by NoopDataStorageListener {

    private val log = logger {}

    val activtiesTableColumns = listOf<TableColumn<Activity>>(
        tableColumnVenueImage { it.venue.imageFileName },
        TableColumn("Name", ColSize.Weight(0.5f), TextRenderer({ it.name }, { it.name.lowercase() })),
        TableColumn("Venue", ColSize.Weight(0.5f), TextRenderer { it.venue.name }),
        TableColumn("Date", ColSize.Width(200.dp), TextRenderer { it.dateTimeRange.prettyPrint(clock.today().year) }),
        TableColumn("Rating", ColSize.Width(120.dp), TextRenderer { it.venue.rating.string }),
        tableColumnFavorited { it.venue.isFavorited },
        tableColumnWishlisted { it.venue.isWishlisted },
        TableColumn("Bkd", ColSize.Width(30.dp), TextRenderer { if (it.isBooked) Icons.Lsc.booked else "" }),
        // teacher
        // Checkins count
    )


    private val _allActivities = mutableStateListOf<Activity>()
    val allActivities: List<Activity> = _allActivities
    private val _activities = mutableStateListOf<Activity>()
    val activities: List<Activity> = _activities

    private val _selectedActivity = MutableStateFlow<Activity?>(null)
    val selectedActivity = _selectedActivity.asStateFlow()

    val selectedVenue: StateFlow<Venue?> = selectedActivity.map { activity ->
        activity?.venue?.let { simpleVenue ->
            dataStorage.selectVenueById(simpleVenue.id).also { venue ->
                venueEdit.init(venue)
            }
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
        venueEdit.updatePropertiesOf(selectedVenue.value!!)
        dataStorage.update(selectedVenue.value!!)
    }

    override fun onVenueUpdated(venue: Venue) {
        venueEdit.init(venue)
    }

    private fun resetActivities() {
        _activities.clear()
        _activities.addAll(_allActivities.filter { searching.matches(it) }.let {
            sorting.sortIt(it)
        })
    }
}
