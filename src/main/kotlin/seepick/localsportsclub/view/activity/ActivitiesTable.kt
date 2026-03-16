package seepick.localsportsclub.view.activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
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
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.shared.CategoryColumn
import seepick.localsportsclub.view.shared.CheckedinColumn
import seepick.localsportsclub.view.shared.DistanceColumn
import seepick.localsportsclub.view.shared.PlanColumn
import seepick.localsportsclub.view.shared.RatingColumn
import seepick.localsportsclub.view.shared.VenueColumn

fun activitiesTableColumns(clock: Clock) = listOf<TableColumn<Activity>>(
    tableColumnVenueImage { it.venue },
    TableColumn(
        VisualIndicator.StringIndicator("Name"),
        WidthOrWeight.Weight(0.6f),
        CellRenderer.TextRenderer(valueExtractor = { activity ->
            buildString {
                if (activity.state == ActivityState.Booked) {
                    append("${Lsc.icons.reservedEmoji} ")
                }
                if (activity.remarkRating != null) {
                    append("${activity.remarkRating.emoji} ")
                }
                append(activity.name)
                if (activity.teacher != null) {
                    append(" /${activity.teacher}")
                    if (activity.teacherRemarkRating != null) {
                        append(" ${activity.teacherRemarkRating.emoji}")
                    }
                }
            }
        }, sortExtractor = { (if (it.teacher == null) it.name else "${it.name} /${it.teacher}").lowercase() })
    ),
    VenueColumn(),
    CategoryColumn(),
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
//    tableColumnFavorited { it.venue.isFavorited },
//    tableColumnWishlisted { it.venue.isWishlisted },
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
        columns = viewModel.tableColumns,
        allItemsCount = viewModel.allItems.size,
        items = viewModel.items,
        customTableItemBgColorEnabled = true,
        onItemClicked = viewModel::onActivitySelected,
        onHeaderClicked = viewModel.sorting::onSortColumn,
        onItemNavigation = viewModel::onItemNavigation,
        sortColumn = viewModel.sorting.sortColumn,
        sortDirection = viewModel.sorting.sortDirection,
        selectedItem = selectedActivity,
    )
}
