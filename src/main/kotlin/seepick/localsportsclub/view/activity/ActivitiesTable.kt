package seepick.localsportsclub.view.activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyFromShorterPrint
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.CellValue
import seepick.localsportsclub.view.common.table.MainTable
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
        header = VisualIndicator.StringIndicator("Name"),
        size = WidthOrWeight.Weight(0.6f),
        renderer = CellRenderer.TextRenderer(
            sortExtractor = { (if (it.teacher == null) it.name else "${it.name} /${it.teacher}").lowercase() },
            paddingLeft = true,
            valueExtractor = { activity ->
                CellValue(buildAnnotatedString {
                    if (activity.state == ActivityState.Booked) {
                        append("${Lsc.icons.reservedEmoji} ")
                    }
                    appendRatedName(activity)
                    appendRatedTeacher(activity)
                })
            },
        )
    ),
    VenueColumn("Venue"),
    CategoryColumn(),
    TableColumn(
        VisualIndicator.StringIndicator("Date"), WidthOrWeight.Width(100.dp), CellRenderer.TextRenderer(
            valueExtractor = { CellValue(it.dateTimeRange.prettyFromShorterPrint(clock.today().year)) },
            sortExtractor = { it.dateTimeRange },
            textAlign = TextAlign.Right,
        )
    ),
    CheckedinColumn(paddingRight = true),
    PlanColumn(),
    RatingColumn(paddingRight = true),
    DistanceColumn(),
)

@Composable
fun ActivitiesTable(
    viewModel: ActivityViewModel = koinViewModel(),
) {
    val selectedSubEntity by viewModel.selectedSubEntity.collectAsState()
    val selectedActivity = selectedSubEntity?.maybeActivity

    MainTable(
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
