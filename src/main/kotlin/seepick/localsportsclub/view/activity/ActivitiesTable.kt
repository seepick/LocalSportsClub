package seepick.localsportsclub.view.activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.common.table.Table

@Composable
fun ActivitiesTable(
    viewModel: ActivityViewModel = koinViewModel(),
) {
    val selectedActivity by viewModel.selectedActivity.collectAsState()
    Table(
        itemsLabel = "activities",
        allItemsCount = viewModel.allItems.size,
        items = viewModel.items,
        onItemClicked = viewModel::onActivitySelected,
        onHeaderClicked = viewModel.sorting::onHeaderClicked,
        columns = viewModel.tableColumns,
        sortColumn = viewModel.sorting.sortColumn,
        selectedItem = selectedActivity,
    )
}
