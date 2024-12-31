package seepick.localsportsclub.view.common

import androidx.compose.runtime.mutableStateListOf
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
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.service.SortingDelegate
import seepick.localsportsclub.service.findIndexFor
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.DataStorageListener
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.NoopDataStorageListener
import seepick.localsportsclub.service.model.SimpleVenue
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.search.AbstractSearch
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.venue.detail.VenueEditModel

interface ScreenItem {
    val venue: SimpleVenue
}

abstract class ScreenViewModel<ITEM : ScreenItem, SEARCH : AbstractSearch<ITEM>>(
    private val dataStorage: DataStorage,
) : ViewModel(), DataStorageListener by NoopDataStorageListener, ApplicationLifecycleListener {

    private val log = logger {}

    abstract val tableColumns: List<TableColumn<ITEM>>

    private val _allItems = mutableStateListOf<ITEM>()
    val allItems: List<ITEM> = _allItems
    private val _items = mutableStateListOf<ITEM>()
    val items: List<ITEM> = _items
    abstract val selectedItem: StateFlow<ITEM?>

    private val _selectedFreetraining = MutableStateFlow<Freetraining?>(null)
    val selectedFreetraining = _selectedFreetraining.asStateFlow()
    private val _selectedActivity = MutableStateFlow<Activity?>(null)
    val selectedActivity: StateFlow<Activity?> = _selectedActivity.asStateFlow()

    abstract fun buildSearch(resetItems: () -> Unit): SEARCH

    val searching by lazy { buildSearch(::resetItems) }
    val sorting by lazy { SortingDelegate(tableColumns, resetSort = ::resetItems) }

    val venueEdit = VenueEditModel()

    abstract val selectedVenue: StateFlow<Venue?>

    protected val selectedVenueBySelectedItem by lazy {
        selectedItem.map { item ->
            item?.venue?.let { simpleVenue ->
                dataStorage.selectVenueById(simpleVenue.id).also { venue ->
                    venueEdit.init(venue)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = null
        )
    }

    abstract fun DataStorage.selectAllItems(): List<ITEM>

    override fun onStartUp() {
        log.info { "Filling initial data for: ${this::class.simpleName}" }
        _allItems.addAll(dataStorage.selectAllItems())
        resetItems()
    }

    protected fun onItemAdded(item: ITEM) {
        _allItems.add(item)
        if (searching.matches(item)) {
            val index = findIndexFor(_items, item, sorting.selectedColumnValueExtractor)
            _items.add(index, item)
        }
    }

    open fun onItemSelected(item: SelectedItemType) {
    }

    fun onVenueSelected(venue: Venue) {
        log.trace { "Selected: $venue" }
        viewModelScope.launch {
            // venue can only be clicked in VenueViewModel
            @Suppress("UNCHECKED_CAST")
            (selectedVenue as MutableStateFlow<Venue>).value = venue
            venueEdit.init(venue)
            onItemSelected(VenueSelected(venue))
        }
        _selectedActivity.value = null
        _selectedFreetraining.value = null
    }

    fun onActivitySelected(activity: Activity) {
        log.trace { "Selected: $activity" }
        viewModelScope.launch {
            _selectedActivity.value = activity
            onItemSelected(ActivitySelected(activity))
        }
    }

    fun onFreetrainingSelected(freetraining: Freetraining) {
        log.trace { "Selected: $freetraining" }
        viewModelScope.launch {
            _selectedFreetraining.value = freetraining
            onItemSelected(FreetrainingSelected(freetraining))
        }
    }

    override fun onVenueUpdated(venue: Venue) {
        venueEdit.init(venue)
    }

    fun updateVenue() {
        venueEdit.updatePropertiesOf(selectedVenue.value!!)
        dataStorage.update(selectedVenue.value!!)
    }

    private fun resetItems() {
        _items.clear()
        _items.addAll(_allItems.filter { searching.matches(it) }.let {
            sorting.sortIt(it)
        })
    }
}

sealed interface SelectedItemType
class VenueSelected(val venue: Venue) : SelectedItemType
class ActivitySelected(val activity: Activity) : SelectedItemType
class FreetrainingSelected(val freetraining: Freetraining) : SelectedItemType
