package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn

@Composable
fun SimpleFreetrainingsTable(
    freetrainings: SnapshotStateList<Freetraining>,
    selectedFreetraining: Freetraining? = null,
    onFreetrainingClicked: ((Freetraining) -> Unit)?,
    clock: Clock = koinInject(),
) {
    if (freetrainings.isEmpty()) {
        Text("No Freetrainings.")
    } else {
        val currentYear = clock.today().year
        Text("Freetrainings:")
        Row {
            Table(
                items = freetrainings,
                headerEnabled = false,
                selectedItem = selectedFreetraining,
                modifier = Modifier.height(150.dp),
                columns = listOf(
                    TableColumn(
                        size = ColSize.Width(20.dp),
                        renderer = CellRenderer.TextRenderer {
                            buildString {
                                if (it.wasCheckedin) append(Icons.Lsc.checkedin)
                            }
                        }
                    ),
                    TableColumn(
                        size = ColSize.Width(170.dp),
                        renderer = CellRenderer.TextRenderer { it.date.prettyPrint(currentYear) },
                    ),
                    TableColumn(
                        size = ColSize.Weight(1.0f),
                        renderer = CellRenderer.TextRenderer { it.name },
                    )
                ),
                onItemClicked = onFreetrainingClicked,
                sortColumn = null,
            )
        }
    }
}
