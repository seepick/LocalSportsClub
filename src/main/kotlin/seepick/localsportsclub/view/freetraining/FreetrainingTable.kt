package seepick.localsportsclub.view.freetraining

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted

fun freetrainingsTableColumns(clock: Clock) = listOf<TableColumn<Freetraining>>(
    tableColumnVenueImage { it.venue.imageFileName },
    TableColumn("Name", ColSize.Weight(0.5f), CellRenderer.TextRenderer({ it.name }, { it.name.lowercase() })),
    TableColumn("Venue", ColSize.Weight(0.5f), CellRenderer.TextRenderer { it.venue.name }),
    TableColumn("Category", ColSize.Width(150.dp), CellRenderer.TextRenderer { it.category }),
    TableColumn(
        "Date",
        ColSize.Width(100.dp),
        CellRenderer.TextRenderer(extractor = { it.date.prettyPrint(clock.today().year) }, sortExtractor = { it.date })
    ),
    TableColumn("Rating", ColSize.Width(90.dp), CellRenderer.TextRenderer { it.venue.rating.string }),
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
        allItemsCount = viewModel.allItems.size,
        columns = viewModel.tableColumns,
        sortColumn = viewModel.sorting.sortColumn,
        onItemClicked = viewModel::onFreetrainingSelected,
        onHeaderClicked = viewModel.sorting::onHeaderClicked,
        columnModifier = Modifier.padding(bottom = 20.dp),
    )
}
