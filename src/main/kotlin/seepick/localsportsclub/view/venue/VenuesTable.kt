package seepick.localsportsclub.view.venue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.common.table.Table

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
