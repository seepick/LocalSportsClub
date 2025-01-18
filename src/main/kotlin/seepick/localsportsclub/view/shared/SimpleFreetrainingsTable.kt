package seepick.localsportsclub.view.shared

import androidx.compose.foundation.border
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.SortDirection
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn

@Composable
fun SimpleFreetrainingsTable(
    freetrainings: SnapshotStateList<Freetraining>,
    selectedFreetraining: Freetraining? = null,
    onFreetrainingClicked: ((Freetraining) -> Unit)?,
    clock: Clock = koinInject(),
    modifier: Modifier = Modifier,
) {
    if (freetrainings.isEmpty()) {
        Text("No Freetrainings.")
    } else {
        val currentYear = clock.today().year
        Text("Freetrainings:")
        Table(
            items = freetrainings,
            headerEnabled = false,
            selectedItem = selectedFreetraining,
            boxModifier = Modifier.border(1.dp, Lsc.colors.onSurface).then(modifier),
            columns = listOf(
                TableColumn(
                    size = WidthOrWeight.Width(170.dp),
                    renderer = CellRenderer.TextRenderer { it.date.prettyPrint(currentYear) },
                ),
                TableColumn(
                    size = WidthOrWeight.Weight(1.0f),
                    renderer = CellRenderer.TextRenderer {
                        "${it.state.iconStringAndSuffix()}${it.name}"
                    },
                )
            ),
            onItemClicked = onFreetrainingClicked,
            sortColumn = null,
            sortDirection = SortDirection.Asc, // doesnt matter
        )
    }
}
