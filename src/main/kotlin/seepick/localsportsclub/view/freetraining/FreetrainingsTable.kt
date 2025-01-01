package seepick.localsportsclub.view.freetraining

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted

fun freetrainingsTableColumns(clock: Clock) = listOf<TableColumn<Freetraining>>(
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

@Composable
fun FreetrainingsTable(
    viewModel: FreetrainingViewModel = koinViewModel(),
) {
    val selectedFreetraining by viewModel.selectedFreetraining.collectAsState()
    Table(
        itemsLabel = "freetrainings",
        items = viewModel.items,
        selectedItem = selectedFreetraining,
        allItemsCount = viewModel.allItems.size,
        columns = viewModel.tableColumns,
        sortColumn = viewModel.sorting.sortColumn,
        onItemClicked = viewModel::onFreetrainingSelected,
        onHeaderClicked = viewModel.sorting::onHeaderClicked,
    )
}
