package seepick.localsportsclub.view.freetraining

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Freetraining
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
import seepick.localsportsclub.view.shared.RatingColumn
import seepick.localsportsclub.view.shared.VenueColumn

fun freetrainingsTableColumns(clock: Clock) = listOf<TableColumn<Freetraining>>(
    tableColumnVenueImage { it.venue },
    TableColumn(
        header = VisualIndicator.StringIndicator("Name"),
        size = WidthOrWeight.Weight(0.6f),
        renderer = CellRenderer.TextRenderer(
            valueExtractor = { CellValue(it.name) },
            sortExtractor = { it.name.lowercase() },
            paddingLeft = true,
        )
    ),
    VenueColumn("Venue"),
    CategoryColumn(),
    TableColumn(
        header = VisualIndicator.StringIndicator("Date"),
        size = WidthOrWeight.Width(80.dp),
        renderer = CellRenderer.TextRenderer(
            valueExtractor = { CellValue(it.date.prettyPrint(clock.today().year)) },
            sortExtractor = { it.date },
            textAlign = TextAlign.Right,
        )
    ),
    CheckedinColumn(paddingRight = true),
    RatingColumn(paddingRight = true),
    DistanceColumn(),
//    tableColumnFavorited { it.venue.isFavorited },
//    tableColumnWishlisted { it.venue.isWishlisted },
)


@Composable
fun FreetrainingsTable(
    viewModel: FreetrainingViewModel = koinViewModel(),
) {
    val selectedSubEntity by viewModel.selectedSubEntity.collectAsState()
    val selectedFreetraining = selectedSubEntity?.maybeFreetraining

    MainTable(
        itemsLabel = "freetrainings",
        items = viewModel.items,
        selectedItem = selectedFreetraining,
        customTableItemBgColorEnabled = true,
        allItemsCount = viewModel.allItems.size,
        columns = viewModel.tableColumns,
        sortColumn = viewModel.sorting.sortColumn,
        sortDirection = viewModel.sorting.sortDirection,
        onItemClicked = viewModel::onFreetrainingSelected,
        onItemNavigation = viewModel::onItemNavigation,
        onHeaderClicked = viewModel.sorting::onSortColumn,
    )
}
