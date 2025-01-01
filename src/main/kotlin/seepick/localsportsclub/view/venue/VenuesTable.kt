package seepick.localsportsclub.view.venue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.CellRenderer.TextRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableCell
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted

fun venuesTableColumns() = listOf<TableColumn<Venue>>(
    tableColumnVenueImage { it.imageFileName },
    TableColumn("Name", ColSize.Weight(0.7f),
        CellRenderer.CustomRenderer { venue, col ->
            TableCell(
                text = venue.name,
                size = col.size,
                textDecoration = if (venue.isDeleted) TextDecoration.LineThrough else null,
            )
        }, sortValueExtractor = { it.name.lowercase() }
    ),
    TableColumn("Acts", ColSize.Width(100.dp), TextRenderer { it.activities.size }),
    TableColumn(
        "Chks",
        ColSize.Width(100.dp),
        TextRenderer { it.activities.filter { it.wasCheckedin }.size + it.freetrainings.filter { it.wasCheckedin }.size }),
    TableColumn("Rating", ColSize.Width(150.dp), TextRenderer { it.rating.string }),
    tableColumnFavorited { it.isFavorited },
    tableColumnWishlisted { it.isWishlisted },
)

@Composable
fun VenuesTable(
    viewModel: VenueViewModel = koinViewModel(),
) {
    val selectedVenue by viewModel.selectedVenue.collectAsState()
    Table(
        itemsLabel = "venues",
        allItemsCount = viewModel.allItems.size,
        items = viewModel.items,
        onItemClicked = viewModel::onVenueSelected,
        onHeaderClicked = viewModel.sorting::onHeaderClicked,
        columns = viewModel.tableColumns,
        sortColumn = viewModel.sorting.sortColumn,
        selectedItem = selectedVenue,
    )
}
