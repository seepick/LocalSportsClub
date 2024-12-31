package seepick.localsportsclub.view.freetraining

import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.view.common.ScreenViewModel
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted

class FreetrainingViewModel(
    private val clock: Clock,
    dataStorage: DataStorage,
) : ScreenViewModel<Freetraining, FreetrainingsSearch>(dataStorage) {

    override val tableColumns = listOf<TableColumn<Freetraining>>(
        tableColumnVenueImage { it.venue.imageFileName },
        TableColumn("Name", ColSize.Weight(0.5f), CellRenderer.TextRenderer({ it.name }, { it.name.lowercase() })),
        TableColumn("Venue", ColSize.Weight(0.5f), CellRenderer.TextRenderer { it.venue.name }),
        TableColumn("Category", ColSize.Width(170.dp), CellRenderer.TextRenderer { it.category }),
        TableColumn("Date",
            ColSize.Width(120.dp),
            CellRenderer.TextRenderer { it.date.prettyPrint(clock.today().year) }),

        TableColumn("Rating", ColSize.Width(120.dp), CellRenderer.TextRenderer { it.venue.rating.string }),
        tableColumnFavorited { it.venue.isFavorited },
        tableColumnWishlisted { it.venue.isWishlisted },
        // TODO: checkins count
    )

    override val selectedItem = selectedFreetraining
    override val selectedVenue = selectedVenueBySelectedItem

    override fun buildSearch(resetItems: () -> Unit) = FreetrainingsSearch(resetItems)

    override fun DataStorage.selectAllItems() = selectAllFreetrainings()

    override fun onFreetrainingsAdded(freetrainings: List<Freetraining>) {
        freetrainings.forEach { freetraining ->
            onItemAdded(freetraining)
        }
    }
}
