package seepick.localsportsclub.view.venue

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.service.SortingDelegate
import seepick.localsportsclub.service.findIndexFor
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.DataStorageListener
import seepick.localsportsclub.service.model.NoopDataStorageListener
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.search.VenueSearch
import seepick.localsportsclub.view.common.table.CellRenderer.TextRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted
import seepick.localsportsclub.view.venue.detail.VenueEditModel

class VenueViewModel(
    private val dataStorage: DataStorage,
) : ViewModel(), DataStorageListener by NoopDataStorageListener {

    private val log = logger {}

    val venuesTableColumns = listOf<TableColumn<Venue>>(
        tableColumnVenueImage { it.imageFileName },
        TableColumn("Name", ColSize.Weight(0.7f), TextRenderer({ it.name }, { it.name.lowercase() })),
        TableColumn("Slug", ColSize.Weight(0.3f), TextRenderer { it.slug }),
        TableColumn("Activities", ColSize.Width(100.dp), TextRenderer { it.activities.size }),
        TableColumn("Checkins", ColSize.Width(100.dp), TextRenderer { it.activities.filter { it.wasCheckedin }.size }),
        TableColumn("Rating", ColSize.Width(150.dp), TextRenderer { it.rating.string }),
        tableColumnFavorited { it.isFavorited },
        tableColumnWishlisted { it.isWishlisted },
    )

    private val _allVenues = mutableStateListOf<Venue>()
    val allVenues: List<Venue> = _allVenues
    private val _venues = mutableStateListOf<Venue>()
    val venues: List<Venue> = _venues
    var selectedVenue by mutableStateOf<Venue?>(null)
        private set
    var selectedActivity by mutableStateOf<Activity?>(null)
        private set
    val searching = VenueSearch(::resetVenues)
    val sorting = SortingDelegate(venuesTableColumns, resetSort = ::resetVenues)
    val venueEdit = VenueEditModel()

    fun onStartUp() {
        log.info { "On startup: Filling initial data." }
        _allVenues.addAll(dataStorage.selectAllVenues())
        resetVenues()
    }

    override fun onVenueAdded(venue: Venue) {
        _allVenues.add(venue)
        if (searching.matches(venue)) {
            val index = findIndexFor(_venues, venue, sorting.selectedColumnValueExtractor)
            _venues.add(index, venue)
        }
    }

    override fun onVenueUpdated(venue: Venue) {
        venueEdit.init(venue)
    }

    fun updateVenue() {
        venueEdit.updatePropertiesOf(selectedVenue!!)
        dataStorage.update(selectedVenue!!)
    }

    fun onVenueClicked(venue: Venue) {
        log.trace { "Selected: $venue" }
        selectedVenue = venue
        venueEdit.init(venue)
        selectedActivity = null
    }

    fun onActivitySelected(activity: Activity) {
        selectedActivity = activity
    }

    private fun resetVenues() {
        _venues.clear()
        _venues.addAll(_allVenues.filter { searching.matches(it) }.let {
            sorting.sortIt(it)
        })
    }
}
