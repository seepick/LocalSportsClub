package seepick.localsportsclub.view.venue

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.common.table.Table

@Composable
fun RowScope.VenuesTable(
    viewModel: VenueViewModel = koinViewModel(),
) {
    Table(
        itemsLabel = "venues",
        allItemsCount = viewModel.allVenues.size,
        items = viewModel.venues,
        onItemClicked = { viewModel.onVenueClicked(it) },
        onHeaderClicked = { viewModel.sorting.onHeaderClicked(it) },
        columns = viewModel.venuesTableColumns,
        sortColumn = viewModel.sorting.sortColumn,
        selectedItem = viewModel.selectedVenue,
    )
}
