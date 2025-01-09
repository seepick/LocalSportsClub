package seepick.localsportsclub.view.activity

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted

fun activitiesTableColumns(clock: Clock) = listOf<TableColumn<Activity>>(
    tableColumnVenueImage { it.venue.imageFileName },
    TableColumn(
        "Name",
        ColSize.Weight(0.5f),
        CellRenderer.TextRenderer({ it.nameWithTeacherIfPresent }, { it.nameWithTeacherIfPresent.lowercase() })
    ),
    TableColumn("Venue", ColSize.Weight(0.5f), CellRenderer.TextRenderer { it.venue.name }),
    TableColumn("Category", ColSize.Width(150.dp), CellRenderer.TextRenderer { it.category }),
    TableColumn(
        "Date",
        ColSize.Width(200.dp),
        CellRenderer.TextRenderer(
            extractor = { it.dateTimeRange.prettyPrint(clock.today().year) },
            sortExtractor = { it.dateTimeRange })
    ),
    TableColumn("Rating", ColSize.Width(120.dp), CellRenderer.TextRenderer { it.venue.rating.string }),
    tableColumnFavorited { it.venue.isFavorited },
    tableColumnWishlisted { it.venue.isWishlisted },
    TableColumn("Bkd", ColSize.Width(30.dp), CellRenderer.TextRenderer { if (it.isBooked) Icons.Lsc.booked else "" }),

    )

@Composable
fun ActivitiesTable(
    viewModel: ActivityViewModel = koinViewModel(),
) {
    val selectedSubEntity by viewModel.selectedSubEntity.collectAsState()
    val selectedActivity = selectedSubEntity?.maybeActivity

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
