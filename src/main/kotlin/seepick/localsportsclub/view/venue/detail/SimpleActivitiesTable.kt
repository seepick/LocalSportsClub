package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.service.Clock
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.prettyPrint
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn

@Composable
fun SimpleActivitiesTable(
    activities: List<Activity>,
    selectedActivity: Activity? = null,
    onActivityClicked: (Activity) -> Unit,
    clock: Clock = koinInject(),
) {
    if (activities.isEmpty()) {
        Text("No activities.")
    } else {
        val currentYear = clock.today().year
        Text("Activities:")
        Row {
            Table(
                items = activities,
                headerEnabled = false,
                selectedItem = selectedActivity,
                modifier = Modifier.height(150.dp),
                columns = listOf(
                    TableColumn(
                        size = ColSize.Width(20.dp),
                        renderer = CellRenderer.TextRenderer {
                            buildString {
                                if (it.isBooked) append(Icons.Lsc.booked)
                                if (it.wasCheckedin) append(Icons.Lsc.checkedin)
                            }
                        }
                    ),
                    TableColumn(
                        size = ColSize.Width(170.dp),
                        renderer = CellRenderer.TextRenderer { it.dateTimeRange.prettyPrint(currentYear) },
                    ),
                    TableColumn(
                        size = ColSize.Weight(1.0f),
                        renderer = CellRenderer.TextRenderer { it.name },
                    )
                ),
                onItemClicked = {
                    onActivityClicked(it)
                },
                sortColumn = null,
            )
        }
    }
}
