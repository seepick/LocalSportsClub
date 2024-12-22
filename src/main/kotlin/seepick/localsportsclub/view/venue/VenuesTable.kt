package seepick.localsportsclub.view.venue

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.CellRenderer.TextRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn

val venuesTableColumns = listOf<TableColumn<Venue>>(
    TableColumn("Image", ColSize.Width(150.dp), CellRenderer.CustomRenderer { venue, colSize ->
        Row(Modifier.height(30.dp)) {
            VenueImage(venue.imageFileName)
        }
    }, sortingEnabled = false),
    TableColumn("Name", ColSize.Weight(0.7f), TextRenderer { it.name }),
    TableColumn("Slug", ColSize.Weight(0.3f), TextRenderer { it.slug }),
    TableColumn("Rating", ColSize.Width(150.dp), TextRenderer { it.rating.string }),
//    TableColumn("Rating2", ColSize.Width(150.dp), CellRenderer.CustomRenderer { venue, colSize ->
//        Row(Modifier.let { applyColSize(it, colSize) }) {
//            repeat(venue.rating.value) {
//                Icon(Icons.Filled.Add, null)
//            }
//        }
//    }, valueExtractor = { it.rating as Comparable<Any> }),
)

@Composable
fun RowScope.VenuesTable(
    viewModel: VenueViewModel = koinViewModel(),
) {
    Table(
        itemsLabel = "venues",
        allItemsCount = viewModel.allVenues.size,
        items = viewModel.venues,
        onItemClicked = { viewModel.onVenueClicked(it) },
        onHeaderClicked = { viewModel.updateSorting(it) },
        columns = venuesTableColumns,
        sortColumn = viewModel.sortColumn,
        selectedItem = viewModel.selectedVenue,
    )
}
