package seepick.localsportsclub.view.venue

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.DataStorageListener
import seepick.localsportsclub.service.model.Rating
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.searchIndexFor
import seepick.localsportsclub.view.common.table.TableColumn

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
    }
}

class VenueViewModel(
    private val dataStorage: DataStorage,
) : ViewModel(), DataStorageListener {

    private val log = logger {}
    private val _allVenues = mutableStateListOf<Venue>()
    val allVenues: List<Venue> = _allVenues
    private val _venues = mutableStateListOf<Venue>()
    val venues: List<Venue> = _venues
    var selectedVenue by mutableStateOf<Venue?>(null)
        private set
    val venueEdit = VenueEditModel()
    private val searching = VenueSearch()
    var sortColumn: TableColumn<Venue> by mutableStateOf(venuesTableColumns.first { it.sortingEnabled })
        private set

    fun onStartUp() {
        log.info { "On startup: Filling initial data." }
        log.warn { "Just a test" }
//        _allVenues.addAll(DummyDataGenerator.generateVenues(40))
        _allVenues.addAll(dataStorage.selectAllVenues())
        resetVenues()
    }

    fun onVenueClicked(venue: Venue) {
        log.trace { "Selected venue: $venue" }
        selectedVenue = venue
        venueEdit.init(venue)
    }

    fun setSearchTerm(term: String) {
        term.trim().also {
            if (it.isEmpty()) {
                searching.clearTerm()
            } else {
                searching.setTerm(it)
            }
        }
        resetVenues()
    }

    fun onHeaderClicked(column: TableColumn<Venue>) {
        if (sortColumn == column) return
        require(sortColumn.sortingEnabled)
        log.debug { "update sorting for: ${column.headerLabel}" }
        sortColumn = column
        resetVenues()
    }

    override fun onVenueAdded(venue: Venue) {
        _allVenues.add(venue)
        if (searching.matches(venue)) {
            val index = searchIndexFor(_venues, venue, sortColumn.sortValueExtractor!!)
            _venues.add(index, venue)
        }
    }

    private fun resetVenues() {
        _venues.clear()
        _venues.addAll(_allVenues.filter { searching.matches(it) }.let {
            it.sortedBy { venue ->
                sortColumn.sortValueExtractor!!.invoke(venue)
            }
        })
    }

    fun updateVenue() {
        selectedVenue!!.also { venue ->
            venueEdit.updatePropertiesOf(venue)
            log.debug { "Updating venue: $venue" }
            dataStorage.update(venue)
        }
    }
}
