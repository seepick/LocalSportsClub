package seepick.localsportsclub.view.venue

import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.search.VenueSearch
import seepick.localsportsclub.view.common.ScreenViewModel
import seepick.localsportsclub.view.common.SelectedItemType
import seepick.localsportsclub.view.common.VenueSelected
import seepick.localsportsclub.view.common.table.CellRenderer.TextRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted

class VenueViewModel(
    dataStorage: DataStorage,
) : ScreenViewModel<Venue, VenueSearch>(dataStorage) {

    override val tableColumns = listOf<TableColumn<Venue>>(
        tableColumnVenueImage { it.imageFileName },
        TableColumn("Name", ColSize.Weight(0.7f), TextRenderer({ it.name }, { it.name.lowercase() })),
        TableColumn("Slug", ColSize.Weight(0.3f), TextRenderer { it.slug }),
        TableColumn("Activities", ColSize.Width(100.dp), TextRenderer { it.activities.size }),
        TableColumn("Checkins", ColSize.Width(100.dp), TextRenderer { it.activities.filter { it.wasCheckedin }.size }),
        TableColumn("Rating", ColSize.Width(150.dp), TextRenderer { it.rating.string }),
        tableColumnFavorited { it.isFavorited },
        tableColumnWishlisted { it.isWishlisted },
    )

    override val selectedVenue = MutableStateFlow<Venue?>(null)
    override val selectedItem = selectedVenue

    override fun onItemSelected(item: SelectedItemType) {
        if (item is VenueSelected) {
            selectedVenue.value = item.venue
        }
    }

    override fun buildSearch(resetItems: () -> Unit) = VenueSearch(resetItems)

    override fun DataStorage.selectAllItems() =
        selectAllVenues()

    override fun onVenueAdded(venue: Venue) {
        onItemAdded(venue)
    }
}
