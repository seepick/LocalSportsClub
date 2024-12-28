package seepick.localsportsclub.view.activity

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.view.common.table.CellRenderer.TextRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn


val activtiesTableColumns = listOf<TableColumn<Activity>>(
    TableColumn("ID", ColSize.Width(70.dp), TextRenderer { it.id }),
//    TableColumn("Image", ColSize.Width(100.dp), CellRenderer.CustomRenderer { venue, colSize ->
//        Row(Modifier.height(30.dp).width(100.dp)) {
//            VenueImage(venue.imageFileName)
//        }
//    }, sortingEnabled = false),
    TableColumn("Name", ColSize.Weight(0.5f), TextRenderer({ it.name }, { it.name.lowercase() })),
    TableColumn("Venue", ColSize.Weight(0.5f), TextRenderer { it.venue.name }),
//    TableColumn("Activities", ColSize.Width(100.dp), TextRenderer { it.activities.size }),
//    TableColumn("Checkins", ColSize.Width(100.dp), TextRenderer { it.activities.filter { it.wasCheckedin }.size }),
//    TableColumn("Rating", ColSize.Width(150.dp), TextRenderer { it.rating.string }),
//    TableColumn("Rating2", ColSize.Width(150.dp), CellRenderer.CustomRenderer { venue, colSize ->
//        Row(Modifier.let { applyColSize(it, colSize) }) {
//            repeat(venue.rating.value) {
//                Icon(Icons.Filled.Add, null)
//            }
//        }
//    }, valueExtractor = { it.rating as Comparable<Any> }),
)

@Composable
fun RowScope.ActivitiesTable(
    viewModel: ActivityViewModel = koinViewModel(),
) {
    val selectedActivity by viewModel.selectedActivity.collectAsState()
    Table(
        itemsLabel = "activities",
        allItemsCount = viewModel.allActivities.size,
        items = viewModel.activities,
        onItemClicked = { viewModel.onActivityClicked(it) },
        onHeaderClicked = { viewModel.sorting.onHeaderClicked(it) },
        columns = activtiesTableColumns,
        sortColumn = viewModel.sorting.sortColumn,
        selectedItem = selectedActivity,
    )
}
