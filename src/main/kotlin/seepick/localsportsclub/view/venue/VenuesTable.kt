package seepick.localsportsclub.view.venue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.SortDirection
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.date.prettyShortPrint
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.table.CellRenderer.TextRenderer
import seepick.localsportsclub.view.common.table.CellValue
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.shared.DistanceColumn
import seepick.localsportsclub.view.shared.RatingColumn
import seepick.localsportsclub.view.shared.VenueColumn
import java.time.LocalDate

fun venuesTableColumns(today: LocalDate) = listOf<TableColumn<Venue>>(
    tableColumnVenueImage { it },
    VenueColumn(
        headerLabel = "Name",
        size = WidthOrWeight.Weight(0.7f),
        paddingLeft = true,
    ),
    TableColumn(
        header = Lsc.icons.activitiesIndicator,
        tooltip = "Available activities",
        size = WidthOrWeight.Width(40.dp),
        renderer = TextRenderer.forInt(textAlign = TextAlign.Right) {
            it.activities.filter { !it.isInPast(today) }.size
        },
        initialSortDirection = SortDirection.Desc,
    ),
    TableColumn(
        header = Lsc.icons.freetrainingsIndicator,
        size = WidthOrWeight.Width(40.dp),
        renderer = TextRenderer.forInt(textAlign = TextAlign.Right) {
            it.freetrainings.filter { !it.isInPast(today) }.size
        },
        tooltip = "Available freetrainings",
        initialSortDirection = SortDirection.Desc,
    ),
    TableColumn(
        header = VisualIndicator.EmojiIndicator(LscIcons.reservedEmoji),
        size = WidthOrWeight.Width(30.dp),
        renderer = TextRenderer.forInt(textAlign = TextAlign.Right) {
            it.activities.filter { it.state == ActivityState.Booked }.size + it.freetrainings.filter { it.state == FreetrainingState.Scheduled }.size
        },
        tooltip = "Booked activities/freetrainings",
        initialSortDirection = SortDirection.Desc,
    ),
    TableColumn(
        header = VisualIndicator.EmojiIndicator(LscIcons.checkedinEmoji),
        size = WidthOrWeight.Width(30.dp),
        renderer = TextRenderer.forInt(textAlign = TextAlign.Right) {
            it.activities.filter { it.state == ActivityState.Checkedin }.size +
                    it.freetrainings.filter { it.state == FreetrainingState.Checkedin }.size
        },
        tooltip = "Total past check-ins",
        initialSortDirection = SortDirection.Desc,
    ),
    TableColumn(
        header = VisualIndicator.EmojiIndicator(LscIcons.hiddenEmoji),
        tooltip = "Hidden",
        size = WidthOrWeight.Width(40.dp),
        renderer = TextRenderer.forString(textAlign = TextAlign.Center) {
            if (it.isHidden) LscIcons.hiddenEmoji else ""
        },
        initialSortDirection = SortDirection.Desc,
    ),
    TableColumn(
        header = VisualIndicator.StringIndicator("Visited"),
        tooltip = "The last time you visited the venue",
        size = WidthOrWeight.Width(70.dp),
        renderer = TextRenderer(
            valueExtractor = { CellValue(it.lastVisit()?.prettyShortPrint(SystemClock.today().year) ?: "") },
            sortExtractor = { it.lastVisit() },
            textAlign = TextAlign.Right,
            paddingRight = true,
        ),
        initialSortDirection = SortDirection.Desc,
    ),
    RatingColumn(paddingRight = true),
    DistanceColumn(),
)

@Composable
fun VenuesTable(
    viewModel: VenueViewModel = koinViewModel(),
) {
    val selectedVenue by viewModel.selectedVenue.collectAsState()
    Table(
        itemsLabel = "venues",
        allItemsCount = viewModel.allItems.size,
        items = viewModel.items,
        customTableItemBgColorEnabled = true,
        onItemClicked = viewModel::onVenueSelected,
        onItemNavigation = viewModel::onItemNavigation,
        onHeaderClicked = viewModel.sorting::onSortColumn,
        columns = viewModel.tableColumns,
        sortColumn = viewModel.sorting.sortColumn,
        sortDirection = viewModel.sorting.sortDirection,
        selectedItem = selectedVenue,
    )
}
