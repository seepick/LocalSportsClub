package seepick.localsportsclub.view.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import org.apache.commons.lang3.ObjectUtils.min
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn

@Composable
fun SimpleActivitiesTable(
    activities: List<Activity>,
    selectedActivity: Activity? = null,
    onActivityClicked: ((Activity) -> Unit)?,
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
                modifier = Modifier.height(min(activities.size, 2) * 52.dp),
                columns = listOf(
                    TableColumn(
                        size = ColSize.Width(170.dp),
                        renderer = CellRenderer.TextRenderer { it.dateTimeRange.prettyPrint(currentYear) },
                    ),
                    TableColumn(
                        size = ColSize.Weight(1.0f),
                        renderer = CellRenderer.TextRenderer {
                            buildString {
                                if (it.isBooked) append(Icons.Lsc.booked).append(" ")
                                if (it.wasCheckedin) append(Icons.Lsc.checkedin).append(" ")
                                append(it.name)
                            }
                        },
                    )
                ),
                onItemClicked = onActivityClicked,
                sortColumn = null,
            )
        }
    }
}
