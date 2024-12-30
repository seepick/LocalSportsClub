package seepick.localsportsclub.view.freetraining

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.common.table.Table

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
