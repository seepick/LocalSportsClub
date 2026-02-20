package seepick.localsportsclub.view.activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyFromShorterPrint
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.TableTextCell
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted
import seepick.localsportsclub.view.shared.CheckedinColumn
import seepick.localsportsclub.view.shared.DistanceColumn
import seepick.localsportsclub.view.shared.PlanColumn
import seepick.localsportsclub.view.shared.RatingColumn

fun activitiesTableColumns(clock: Clock) = listOf<TableColumn<Activity>>(
    tableColumnVenueImage { it.venue.imageFileName },
    TableColumn(
        VisualIndicator.StringIndicator("Name"),
        WidthOrWeight.Weight(0.6f),
        CellRenderer.TextRenderer(valueExtractor = { activity ->
            buildString {
                if (activity.state == ActivityState.Booked) {
                    append("${Lsc.icons.reservedEmoji} ")
                }
                append(activity.name)
                if (activity.teacher != null) {
                    append(" /${activity.teacher}")
                }
            }
        }, sortExtractor = { (if (it.teacher == null) it.name else "${it.name} /${it.teacher}").lowercase() })
    ),
    TableColumn(
        VisualIndicator.StringIndicator("Venue"),
        WidthOrWeight.Weight(0.4f),
        sortValueExtractor = { it.venue.name },
        renderer = CellRenderer.CustomRenderer { activity, col ->
            TableTextCell(
                text = activity.venue.name,
                size = col.size,
                textDecoration = if (activity.venue.isDeleted) TextDecoration.LineThrough else null,
            )
        },
    ),
    TableColumn(
        VisualIndicator.StringIndicator("Category"),
        WidthOrWeight.Width(80.dp),
        CellRenderer.TextRenderer { it.category }),
    TableColumn(
        VisualIndicator.StringIndicator("Date"), WidthOrWeight.Width(100.dp), CellRenderer.TextRenderer(
            valueExtractor = { it.dateTimeRange.prettyFromShorterPrint(clock.today().year) },
            sortExtractor = { it.dateTimeRange },
            textAlign = TextAlign.Right,
        )
    ),
    PlanColumn(),
    DistanceColumn(),
    CheckedinColumn(paddingRight = true),
    RatingColumn(),
    tableColumnFavorited { it.venue.isFavorited },
    tableColumnWishlisted { it.venue.isWishlisted },
//    TableColumn(Lsc.icons.booked, WidthOrWeight.Width(30.dp), CellRenderer.TextRenderer(textAlign = TextAlign.Center) {
//        if (it.state == ActivityState.Booked) Lsc.icons.booked else ""
//    }),
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
        customTableItemBgColorEnabled = true,
        onItemClicked = viewModel::onActivitySelected,
        onHeaderClicked = viewModel.sorting::onSortColumn,
        onItemNavigation = viewModel::onItemNavigation,
        columns = viewModel.tableColumns,
        sortColumn = viewModel.sorting.sortColumn,
        sortDirection = viewModel.sorting.sortDirection,
        selectedItem = selectedActivity,
    )
}
