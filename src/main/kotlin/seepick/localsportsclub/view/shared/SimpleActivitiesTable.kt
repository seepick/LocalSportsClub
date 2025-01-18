package seepick.localsportsclub.view.shared

import androidx.compose.foundation.border
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.SortDirection
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.table.CellRenderer
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
            boxModifier = Modifier.border(1.dp, Lsc.colors.onSurface).then(modifier),
            columns = listOf(
                TableColumn(
                    size = WidthOrWeight.Width(170.dp),
                    renderer = CellRenderer.TextRenderer { it.dateTimeRange.prettyPrint(currentYear) },
                ),
                TableColumn(
                    size = WidthOrWeight.Weight(1.0f),
                    renderer = CellRenderer.TextRenderer {
                        "${it.state.iconStringAndSuffix()}${if (it.teacher == null) it.name else "${it.name} /${it.teacher}"}"
                    },
                )
            ),
            onItemClicked = onActivityClicked,
            sortColumn = null,
            sortDirection = SortDirection.Asc, // doesnt matter
        )
    }
}
