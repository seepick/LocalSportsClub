package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.SortDirection
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.TableNavigation


val SimpleActivitiesTable_rowEstimatedHeight = 18

@Composable
fun SimpleActivitiesTable(
    activities: List<Activity>,
    selectedActivity: Activity? = null,
    onActivitySelected: ((Activity) -> Unit)?,
    clock: Clock = koinInject(),
    height: Dp,
    isSyncVenueInProgress: Boolean,
    onSyncVenue: () -> Unit,
    modifier: Modifier = Modifier,
    onItemNavigation: (TableNavigation, Activity) -> Unit,
) {
    if (activities.isEmpty()) {
        Text("No activities.")
    } else {
        val currentYear = clock.today().year
        Row(verticalAlignment = Alignment.Bottom) {
            val syncSize = 30
            Box(contentAlignment = Alignment.Center, modifier = Modifier.width(syncSize.dp).height(syncSize.dp)) {
                val progressSize = syncSize - 10
                if (isSyncVenueInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(progressSize.dp)
                    )
                } else {
                    Tooltip("Sync activity details") {
                        TextButton(onClick = onSyncVenue) {
                            Icon(
                                Lsc.icons.syncActivityDetails, contentDescription = null
                            )
                        }
                    }
                }
            }
            Icon(Lsc.icons.activities, contentDescription = null)
            val today = clock.today()
            val upcomingCount = activities.count { !it.isInPast(today) }
            val pastCount = activities.size - upcomingCount
            val pastText = if (pastCount > 0) " ($pastCount past)" else ""
            Text("$upcomingCount Activities$pastText:")
        }
        Table(
            items = activities,
            headerEnabled = false,
            selectedItem = selectedActivity,
            onItemClicked = onActivitySelected,
            onItemNavigation = onItemNavigation,
            boxModifier = Modifier
                .height(height)
                .then(modifier),
            columns = listOf(
                TableColumn(
                    header = VisualIndicator.NoIndicator,
                    size = WidthOrWeight.Width(190.dp),
                    renderer = CellRenderer.TextRenderer(
                        textAlign = TextAlign.Right,
                        paddingRight = true,
                    ) { it.dateTimeRange.prettyPrint(currentYear) },
                ),
                TableColumn(
                    header = VisualIndicator.NoIndicator,
                    size = WidthOrWeight.Weight(1.0f),
                    renderer = CellRenderer.TextRenderer {
                        "${it.state.iconStringAndSuffix()}${if (it.teacher == null) it.name else "${it.name} /${it.teacher}"}"
                    },
                )
            ),
            sortColumn = null,
            sortDirection = SortDirection.Asc, // doesnt matter; will be ignored
        )
    }
}
