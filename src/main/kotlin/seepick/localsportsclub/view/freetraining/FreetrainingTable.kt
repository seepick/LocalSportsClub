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
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted
import seepick.localsportsclub.view.shared.CheckedinColumn
import seepick.localsportsclub.view.shared.RatingColumn

fun freetrainingsTableColumns(clock: Clock) = listOf<TableColumn<Freetraining>>(
    tableColumnVenueImage { it.venue.imageFileName },
    TableColumn("Name", WidthOrWeight.Weight(0.6f), CellRenderer.TextRenderer({ it.name }, { it.name.lowercase() })),
    TableColumn("Venue", WidthOrWeight.Weight(0.4f), CellRenderer.TextRenderer { it.venue.name }),
    TableColumn("Category", WidthOrWeight.Width(120.dp), CellRenderer.TextRenderer { it.category }),
    TableColumn(
        "Date",
        WidthOrWeight.Width(80.dp),
        CellRenderer.TextRenderer(
            extractor = { it.date.prettyPrint(clock.today().year) },
            sortExtractor = { it.date },
            textAlign = TextAlign.Right,
        )
    ),
    CheckedinColumn(paddingRight = true),
    RatingColumn(),
    tableColumnFavorited { it.venue.isFavorited },
    tableColumnWishlisted { it.venue.isWishlisted },
)


@Composable
fun FreetrainingsTable(
    viewModel: FreetrainingViewModel = koinViewModel(),
) {
    val selectedSubEntity by viewModel.selectedSubEntity.collectAsState()
    val selectedFreetraining = selectedSubEntity?.maybeFreetraining

    Table(
        itemsLabel = "freetrainings",
        items = viewModel.items,
        selectedItem = selectedFreetraining,
        customTableItemBgColorEnabled = true,
        allItemsCount = viewModel.allItems.size,
        columns = viewModel.tableColumns,
        sortColumn = viewModel.sorting.sortColumn,
        sortDirection = viewModel.sorting.sortDirection,
        onItemClicked = viewModel::onFreetrainingSelected,
        onHeaderClicked = viewModel.sorting::onSortColumn,
    )
}
