package seepick.localsportsclub.view.shared

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Activity
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
    modifier: Modifier = Modifier,
) {
    if (activities.isEmpty()) {
        Text("No activities.")
    } else {
        val currentYear = clock.today().year
        Text("Activities:")
        Table(
            items = activities,
            headerEnabled = false,
            selectedItem = selectedActivity,
            boxModifier = modifier,
            columns = listOf(
                TableColumn(
                    size = ColSize.Width(170.dp),
                    renderer = CellRenderer.TextRenderer { it.dateTimeRange.prettyPrint(currentYear) },
                ),
                TableColumn(
                    size = ColSize.Weight(1.0f),
                    renderer = CellRenderer.TextRenderer {
                        "${it.state.iconStringAndSuffix()}${it.name}"
                    },
                )
            ),
            onItemClicked = onActivityClicked,
            sortColumn = null,
        )
    }
}
