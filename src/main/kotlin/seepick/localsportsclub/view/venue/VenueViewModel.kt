package seepick.localsportsclub.view.venue

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.service.SortingDelegate
import seepick.localsportsclub.service.findIndexFor
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.DataStorageListener
import seepick.localsportsclub.service.model.NoopDataStorageListener
import seepick.localsportsclub.service.model.Rating
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.search.VenueSearch

class VenueEditModel {

    var notes = mutableStateOf("")
    var rating = mutableStateOf(Rating.R0)
    var isFavorited by mutableStateOf(false)

    fun init(venue: Venue) {
        notes.value = venue.notes
        rating.value = venue.rating
        isFavorited = venue.isFavorited
    }

    fun updatePropertiesOf(selectedVenue: Venue) {
        selectedVenue.notes = notes.value
        selectedVenue.rating = rating.value
        selectedVenue.isFavorited = isFavorited
        selectedVenue.officialWebsite = selectedVenue.officialWebsite?.let { it.ifEmpty { null } }
    }
}


class VenueViewModel(
    private val dataStorage: DataStorage,
) : ViewModel(), DataStorageListener by NoopDataStorageListener {

    private val log = logger {}
    private val _allVenues = mutableStateListOf<Venue>()
    val allVenues: List<Venue> = _allVenues
    private val _venues = mutableStateListOf<Venue>()
    val venues: List<Venue> = _venues
    var selectedVenue by mutableStateOf<Venue?>(null)
        private set
    val searching = VenueSearch(::resetVenues)
    val sorting = SortingDelegate(venuesTableColumns, resetSort = ::resetVenues)
    val venueEdit = VenueEditModel()

    fun onStartUp() {
        log.info { "On startup: Filling initial data." }
        _allVenues.addAll(dataStorage.selectAllVenues())
        resetVenues()
    }

    fun onVenueClicked(venue: Venue) {
        log.trace { "Selected: $venue" }
        selectedVenue = venue
        venueEdit.init(venue)
    }

    override fun onVenueAdded(venue: Venue) {
        _allVenues.add(venue)
        if (searching.matches(venue)) {
            val index = findIndexFor(_venues, venue, sorting.selectedColumnValueExtractor)
            _venues.add(index, venue)
        }
    }

    private fun resetVenues() {
        _venues.clear()
        _venues.addAll(_allVenues.filter { searching.matches(it) }.let {
            sorting.sortIt(it)
        })
    }

    fun updateVenue() {
        selectedVenue!!.also { venue ->
            venueEdit.updatePropertiesOf(venue)
            dataStorage.update(venue)
        }
    }
}
