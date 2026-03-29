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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.LscColors
import seepick.localsportsclub.service.SortDirection
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.RemarkRating
import seepick.localsportsclub.view.activity.appendRatedName
import seepick.localsportsclub.view.activity.appendRatedTeacher
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.CellValue
import seepick.localsportsclub.view.common.table.Table
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.TableItemBgColor
import seepick.localsportsclub.view.common.table.VDirection
import kotlin.math.abs

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
    onItemNavigation: (VDirection, Activity) -> Unit,
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
                                Lsc.icons.manualSync, contentDescription = null
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
            items = activities.map { SimpleActivity(it) },
            headerEnabled = false,
            selectedItem = selectedActivity?.toSimpleActivity(),
            onItemClicked = { onActivitySelected?.invoke(it.activity) },
            onItemNavigation = { nav, item -> onItemNavigation(nav, item.activity) },
            customTableItemBgColorEnabled = true,
            boxModifier = Modifier.height(height).then(modifier),
            columns = listOf(
                TableColumn(
                    header = VisualIndicator.NoIndicator,
                    size = WidthOrWeight.Width(190.dp),
                    renderer = CellRenderer.TextRenderer.forString(
                        textAlign = TextAlign.Right,
                        paddingRight = true,
                        extractor = { it.activity.dateTimeRange.prettyPrint(currentYear) },
                    ),
                ),
                TableColumn(
                    header = VisualIndicator.NoIndicator,
                    size = WidthOrWeight.Weight(1.0f),
                    renderer = CellRenderer.TextRenderer(
                        sortExtractor = { it.activity.name },
                        valueExtractor = {
                            CellValue(buildAnnotatedString {
                                append(it.activity.state.iconStringAndSuffix())
                                appendRatedName(it.activity)
                                appendRatedTeacher(it.activity)
                            })
                        },
                    ),
                )
            ),
            sortColumn = null,
            sortDirection = SortDirection.Asc, // doesnt matter; will be ignored
        )
    }
}

fun Activity.toSimpleActivity() = SimpleActivity(this)

data class SimpleActivity(
    val activity: Activity,
) : TableItemBgColor {
    override val tableBgColor = Lsc.colors.forSimpleTableBg(activity)
}

fun LscColors.forSimpleTableBg(activity: Activity): Color? {
    return when (activity.state) {
        ActivityState.Blank -> forSimpleTableBgBlank(activity)
        ActivityState.Booked -> Lsc.colors.activityBooked
        ActivityState.Checkedin -> Lsc.colors.activityCheckedin
        ActivityState.Noshow -> Lsc.colors.activityNoShow
        ActivityState.CancelledLate -> Lsc.colors.activityCancelledLate
    }
}

private val ratingMax = RemarkRating.Amazing.weightedValue * 2
private val ratingMin = RemarkRating.Bad.weightedValue * 2
private val ratingRange = ratingMax + abs(ratingMin)

private fun LscColors.forSimpleTableBgBlank(activity: Activity): Color? {
    require(activity.state == ActivityState.Blank)
    if (activity.remarkRating == RemarkRating.Bad || activity.teacherRemarkRating == RemarkRating.Bad) {
        return Lsc.colors.forTableBg(0.0)
    }
    if (activity.remarkRating == null && activity.teacherRemarkRating == null) {
        return null
    }
    val weighted = (activity.remarkRating?.weightedValue ?: 0) + (activity.teacherRemarkRating?.weightedValue ?: 0)
    val weightedAdjusted = weighted + abs(ratingMin)
    val distance = weightedAdjusted.toDouble() / ratingRange.toDouble()
    // log.trace { "name=[$name, teacher=[$teacher], remarkRating=$remarkRating teacherRating=$teacherRemarkRating ... weighted: $weighted; distance: $distance" }
    return Lsc.colors.forTableBg(distance)
}
