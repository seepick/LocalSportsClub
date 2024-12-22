package seepick.localsportsclub.view.venue

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.Rating
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.searchIndexFor
import seepick.localsportsclub.view.common.table.TableColumn

class VenueEditModel {

    var notes = mutableStateOf("")
    var rating by mutableStateOf(Rating.R0)
    var isFavorited by mutableStateOf(false)

    fun init(venue: Venue) {
        notes.value = venue.notes
        rating = venue.rating
        isFavorited = venue.isFavorited
    }

    fun update(selectedVenue: Venue) =
        selectedVenue.copy(
            notes = notes.value,
            rating = rating,
            isFavorited = isFavorited,
        )
}

class VenueViewModel(
    private val dataStorage: DataStorage,
) : ViewModel() {

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
//        _allVenues.addAll(DummyDataGenerator.generateVenues(40))
        _allVenues.addAll(dataStorage.selectVenues())
        resetVenues()
    }

    fun onVenueAdded(venue: Venue) {
        log.debug { "onVenueAdded($venue)" }
        _allVenues.add(venue)
        if (searching.matches(venue)) {
            val index = searchIndexFor(_venues, venue, sortColumn.valueExtractor!!)
            _venues.add(index, venue)
        }
    }

    fun onVenueUpdated(venue: Venue) {
        log.debug { "onVenueUpdated($venue)" }
        _allVenues[_allVenues.indexOfFirst { it.id == venue.id }] = venue
        val index = venues.indexOfFirst { it.id == venue.id }
        if (index != -1) {
            _venues[index] = venue
            selectedVenue = venue
        }
    }

    fun onVenueClicked(venue: Venue) {
        log.trace { "Selected venue: $venue" }
        selectedVenue = venue
        venueEdit.init(venue)
    }

    fun setSearchTerm(term: String) {
        log.debug { "set term [$term]" }
        term.trim().also {
            if (it.isEmpty()) {
                searching.clearTerm()
            } else {
                searching.setTerm(it)
            }
        }
        resetVenues()
    }

    fun updateSorting(column: TableColumn<Venue>) {
        if (sortColumn == column) return
        require(sortColumn.sortingEnabled)
        log.debug { "update sorting for: ${column.headerLabel}" }
        sortColumn = column
        resetVenues()
    }

    private fun resetVenues() {
        _venues.clear()
        _venues.addAll(_allVenues.filter { searching.matches(it) }.let {
            it.sortedBy { venue ->
                sortColumn.valueExtractor!!.invoke(venue)
            }
        })
    }

    fun updateVenue() {
        val updatedVenue = venueEdit.update(selectedVenue!!)
        log.debug { "Updating venue: $updatedVenue" }
        dataStorage.update(updatedVenue)
    }
}
