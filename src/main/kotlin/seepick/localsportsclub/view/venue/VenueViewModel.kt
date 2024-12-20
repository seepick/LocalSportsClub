package seepick.localsportsclub.view.venue

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.domain.Venue
import seepick.localsportsclub.service.DummyDataGenerator
import seepick.localsportsclub.service.searchIndexFor
import seepick.localsportsclub.view.table.TableColumn

class VenueViewModel : ViewModel() {

    private val log = logger {}
    private val allVenues = mutableListOf<Venue>()
    private val _venues = mutableStateListOf<Venue>()
    val venues: List<Venue> = _venues

    private val searching = VenueSearch()
    var sortColumn: TableColumn<Venue> by mutableStateOf(venuesTableColumns.first())
        private set

    fun onStartUp() {
        log.info { "On startup: Filling dummy data." }
        allVenues.addAll(DummyDataGenerator.generateVenues(40))
        resetVenues()
    }

    fun onVenueAdded(venue: Venue) {
        log.debug { "onVenueAdded($venue)" }
        allVenues.add(venue)
        if (searching.matches(venue)) {
            val index = searchIndexFor(_venues, venue, sortColumn.valueExtractor!!)
            _venues.add(index, venue)
        }
    }

    fun onVenueClicked(venue: Venue) {
        // TODO change selectedVenue state
        println("clicked: ${venue.name}")
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
        _venues.addAll(allVenues.filter { searching.matches(it) }.let {
            it.sortedBy { venue ->
                sortColumn.valueExtractor!!.invoke(venue)
            }
        })
    }
}
