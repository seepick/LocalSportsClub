package seepick.localsportsclub.view.venue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.date.prettyShortPrint
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.CellRenderer.TextRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableCell
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted
import seepick.localsportsclub.view.shared.RatingColumn

fun venuesTableColumns() = listOf<TableColumn<Venue>>(
    tableColumnVenueImage { it.imageFileName },
    TableColumn("Name", ColSize.Weight(0.7f), CellRenderer.CustomRenderer { venue, col ->
        TableCell(
            text = venue.name,
            size = col.size,
            textDecoration = if (venue.isDeleted) TextDecoration.LineThrough else null,
        )
    }, sortValueExtractor = { it.name.lowercase() }),
    TableColumn("Act", ColSize.Width(50.dp), TextRenderer { it.activities.size }),
    TableColumn("Fre", ColSize.Width(50.dp), TextRenderer { it.freetrainings.size }),
    TableColumn(LscIcons.checkedin, ColSize.Width(40.dp), TextRenderer {
        it.activities.filter { it.state == ActivityState.Checkedin }.size + it.freetrainings.filter { it.state == FreetrainingState.Checkedin }.size
    }),
    TableColumn(LscIcons.booked, ColSize.Width(40.dp), TextRenderer {
        it.activities.filter { it.state == ActivityState.Booked }.size + it.freetrainings.filter { it.state == FreetrainingState.Scheduled }.size
    }),
    TableColumn(LscIcons.hidden, ColSize.Width(40.dp), TextRenderer {
        if (it.isHidden) LscIcons.hidden else ""
    }),
    TableColumn(
        "Last Visit", ColSize.Width(80.dp), TextRenderer(
            extractor = { it.lastVisit()?.prettyShortPrint(SystemClock.today().year) ?: "" },
            sortExtractor = { it.lastVisit() },
        )
    ),
    TableColumn("km", ColSize.Width(50.dp), TextRenderer { it.distanceInKm?.toString() ?: "" }),
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
        onHeaderClicked = viewModel.sorting::onHeaderClicked,
        columns = viewModel.tableColumns,
        sortColumn = viewModel.sorting.sortColumn,
        selectedItem = selectedVenue,
    )
}
