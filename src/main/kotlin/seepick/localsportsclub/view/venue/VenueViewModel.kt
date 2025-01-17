package seepick.localsportsclub.view.venue

import kotlinx.coroutines.flow.MutableStateFlow
import seepick.localsportsclub.service.BookingService
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.shared.ScreenViewModel
import seepick.localsportsclub.view.shared.SelectedItemType
import seepick.localsportsclub.view.shared.VenueSelected

class VenueViewModel(
    private val dataStorage: DataStorage,
    bookingService: BookingService,
) : ScreenViewModel<Venue, VenueSearch>(dataStorage, bookingService) {

    override val tableColumns = venuesTableColumns()
    override val selectedVenue = MutableStateFlow<Venue?>(null)
    override val selectedItem = selectedVenue
    override val showLinkedVenues = true

    override fun buildSearch(resetItems: () -> Unit) = VenueSearch(dataStorage.venuesCategories, resetItems)
    override fun DataStorage.selectAllItems() = selectVisibleVenues()

    override fun onItemSelected(item: SelectedItemType) {
        if (item is VenueSelected) {
            selectedVenue.value = item.venue
        }
    }

    override fun onVenuesAdded(venues: List<Venue>) {
        onItemsAdded(venues)
    }
}
