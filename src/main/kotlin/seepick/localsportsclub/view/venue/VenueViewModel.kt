package seepick.localsportsclub.view.venue

import kotlinx.coroutines.flow.MutableStateFlow
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.shared.ScreenViewModel
import seepick.localsportsclub.view.shared.SelectedItemType
import seepick.localsportsclub.view.shared.VenueSelected

class VenueViewModel(
    dataStorage: DataStorage,
) : ScreenViewModel<Venue, VenueSearch>(dataStorage) {

    override val tableColumns = venuesTableColumns()
    override val selectedVenue = MutableStateFlow<Venue?>(null)
    override val selectedItem = selectedVenue

    override fun buildSearch(resetItems: () -> Unit) = VenueSearch(resetItems)
    override fun DataStorage.selectAllItems() = selectVisibleVenues()

    override fun onItemSelected(item: SelectedItemType) {
        if (item is VenueSelected) {
            selectedVenue.value = item.venue
        }
    }

    override fun onVenueAdded(venue: Venue) {
        onItemsAdded(listOf(venue))
    }
}
