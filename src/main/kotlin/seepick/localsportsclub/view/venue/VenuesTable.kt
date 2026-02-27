package seepick.localsportsclub.view.venue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.CellRenderer.TextRenderer
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.TableTextCell
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted
import seepick.localsportsclub.view.shared.DistanceColumn
import seepick.localsportsclub.view.shared.RatingColumn
import java.time.LocalDate

fun venuesTableColumns(today: LocalDate) = listOf<TableColumn<Venue>>(
    tableColumnVenueImage { it.imageFileName },
    TableColumn(
        VisualIndicator.StringIndicator("Name"),
        WidthOrWeight.Weight(0.7f),
        CellRenderer.CustomRenderer { venue, col ->
            TableTextCell(
                text = venue.name,
                size = col.size,
                textDecoration = if (venue.isDeleted) TextDecoration.LineThrough else null,
            )
        },
        sortValueExtractor = { it.name.lowercase() }),
    TableColumn(
        Lsc.icons.activitiesIndicator,
        WidthOrWeight.Width(40.dp),
        TextRenderer(textAlign = TextAlign.Right) { it.activities.filter { !it.isInPast(today) }.size },
        tooltip = "Available activities",
        initialSortDirection = SortDirection.Desc,
    ),
    TableColumn(
        header = Lsc.icons.freetrainingsIndicator,
        size = WidthOrWeight.Width(40.dp),
        renderer = TextRenderer(textAlign = TextAlign.Right) {
            it.freetrainings.filter { !it.isInPast(today) }.size
        },
        tooltip = "Available freetrainings",
        initialSortDirection = SortDirection.Desc,
    ),
    TableColumn(
        VisualIndicator.EmojiIndicator(LscIcons.reservedEmoji),
        WidthOrWeight.Width(30.dp),
        TextRenderer(textAlign = TextAlign.Right) {
            it.activities.filter { it.state == ActivityState.Booked }.size + it.freetrainings.filter { it.state == FreetrainingState.Scheduled }.size
        },
        tooltip = "Booked activities/freetrainings",
        initialSortDirection = SortDirection.Desc,
    ),
    TableColumn(
        header = VisualIndicator.EmojiIndicator(LscIcons.checkedinEmoji),
        size = WidthOrWeight.Width(30.dp),
        renderer = TextRenderer(textAlign = TextAlign.Right) {
            it.activities.filter { it.state == ActivityState.Checkedin }.size + it.freetrainings.filter { it.state == FreetrainingState.Checkedin }.size
        },
        tooltip = "Total past check-ins",
        initialSortDirection = SortDirection.Desc,
    ),
    TableColumn(
        header = VisualIndicator.EmojiIndicator(LscIcons.hiddenEmoji),
        tooltip = "Hidden",
        size = WidthOrWeight.Width(40.dp),
        renderer = TextRenderer(textAlign = TextAlign.Center) {
            if (it.isHidden) LscIcons.hiddenEmoji else ""
        },
        initialSortDirection = SortDirection.Desc,
    ),
    TableColumn(
        header = VisualIndicator.StringIndicator("Visited"),
        tooltip = "The last time you visited the venue",
        size = WidthOrWeight.Width(70.dp),
        renderer = TextRenderer(
            valueExtractor = { it.lastVisit()?.prettyShortPrint(SystemClock.today().year) ?: "" },
            sortExtractor = { it.lastVisit() },
            textAlign = TextAlign.Right,
        ),
        initialSortDirection = SortDirection.Desc,
    ),
    DistanceColumn(),
    RatingColumn(),
    tableColumnFavorited { it.isFavorited },
    tableColumnWishlisted { it.isWishlisted },
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
