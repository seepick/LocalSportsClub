package seepick.localsportsclub.view.freetraining

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
import seepick.localsportsclub.service.SortingDelegate
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyShortPrint
import seepick.localsportsclub.service.findIndexFor
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.DataStorageListener
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.NoopDataStorageListener
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted
import seepick.localsportsclub.view.venue.detail.VenueEditModel

class FreetrainingViewModel(
    private val dataStorage: DataStorage,
    private val clock: Clock,
) : ViewModel(), DataStorageListener by NoopDataStorageListener {

    private val log = logger {}

    val freetrainingsTableColumns = listOf<TableColumn<Freetraining>>(
        tableColumnVenueImage { it.venue.imageFileName },
        TableColumn("Name", ColSize.Weight(0.5f), CellRenderer.TextRenderer({ it.name }, { it.name.lowercase() })),
        TableColumn("Venue", ColSize.Weight(0.5f), CellRenderer.TextRenderer { it.venue.name }),
        TableColumn("Category", ColSize.Width(200.dp), CellRenderer.TextRenderer { it.category }),
        TableColumn(
            "Date",
            ColSize.Width(200.dp),
            CellRenderer.TextRenderer { it.date.prettyShortPrint(clock.today().year) }),

        TableColumn("Rating", ColSize.Width(120.dp), CellRenderer.TextRenderer { it.venue.rating.string }),
        tableColumnFavorited { it.venue.isFavorited },
        tableColumnWishlisted { it.venue.isWishlisted },
        // TODO: checkins count
    )

    private val _allFreetrainings = mutableStateListOf<Freetraining>()
    val allFreetrainings: List<Freetraining> = _allFreetrainings
    private val _freetrainings = mutableStateListOf<Freetraining>()
    val freetrainings: List<Freetraining> = _freetrainings

    private val _selectedFreetraining = MutableStateFlow<Freetraining?>(null)
    val selectedFreetraining = _selectedFreetraining.asStateFlow()

    val venueEdit = VenueEditModel()
    val selectedVenue: StateFlow<Venue?> = selectedFreetraining.map { freetraining ->
        freetraining?.venue?.let { simpleVenue ->
            dataStorage.selectVenueById(simpleVenue.id).also { venue ->
                venueEdit.init(venue)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = null
    )

    val searching = FreetrainingsSearch(::resetFreetrainings)
    val sorting = SortingDelegate(freetrainingsTableColumns, resetSort = ::resetFreetrainings)

    fun onStartUp() {
        log.info { "On startup: Filling initial data." }
        _allFreetrainings.addAll(dataStorage.selectAllFreetrainings())
        resetFreetrainings()
    }

    override fun onFreetrainingAdded(freetraining: Freetraining) {
        _allFreetrainings.add(freetraining)
        if (searching.matches(freetraining)) {
            val index = findIndexFor(_freetrainings, freetraining, sorting.selectedColumnValueExtractor)
            _freetrainings.add(index, freetraining)
        }
    }

    fun onFreetrainingClicked(freetraining: Freetraining) {
        log.trace { "Selected: $freetraining" }
        viewModelScope.launch {
            _selectedFreetraining.value = freetraining
        }
    }

    fun updateVenue() {
        venueEdit.updatePropertiesOf(selectedVenue.value!!)
        dataStorage.update(selectedVenue.value!!)
    }

    override fun onVenueUpdated(venue: Venue) {
        venueEdit.init(venue)
    }

    private fun resetFreetrainings() {
        _freetrainings.clear()
        _freetrainings.addAll(_allFreetrainings.filter { searching.matches(it) }.let {
            log.debug { "Reset freetrainings now has: ${it.size}" }
            sorting.sortIt(it)
        })
    }
}
