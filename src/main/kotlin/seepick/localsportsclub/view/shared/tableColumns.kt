package seepick.localsportsclub.view.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.github.seepick.uscclient.plan.Plan
import seepick.localsportsclub.service.SortDirection
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.HasCategory
import seepick.localsportsclub.service.model.HasDistance
import seepick.localsportsclub.service.model.HasPlan
import seepick.localsportsclub.service.model.HasVenue
import seepick.localsportsclub.view.DistanceIndicator
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.common.ModifierWith
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.TableTextCell

fun <T : HasVenue> CheckedinColumn(paddingRight: Boolean = false) = TableColumn<T>(
    header = VisualIndicator.EmojiIndicator(LscIcons.checkedinEmoji),
    size = WidthOrWeight.Width(40.dp),
    renderer = CellRenderer.TextRenderer(textAlign = TextAlign.Right, paddingRight = paddingRight) {
        it.venue.activities.filter { it.state == ActivityState.Checkedin }.size + it.venue.freetrainings.filter { it.state == FreetrainingState.Checkedin }.size
    },
    tooltip = "Check-ins",
    initialSortDirection = SortDirection.Desc,
)

fun <T : HasVenue> RatingColumn() = TableColumn<T>(
    header = VisualIndicator.StringIndicator("Rating"),
    size = WidthOrWeight.Width(90.dp),
    renderer = CellRenderer.TextRenderer(textAlign = TextAlign.Center) { it.venue.rating.label },
    initialSortDirection = SortDirection.Desc,
)

fun <T : HasDistance> DistanceColumn() = TableColumn<T>(
    header = VisualIndicator.StringIndicator("km"),
    size = WidthOrWeight.Width(35.dp),
    sortValueExtractor = { it.distanceInKm },
    renderer = CellRenderer.CustomRenderer { item, col ->
        Row(
            modifier = ModifierWith(col.size).height(30.dp),
        ) {
            DistanceIndicator(item)
        }
    })

fun <T : HasPlan> PlanColumn() = TableColumn<T>(
    header = VisualIndicator.StringIndicator(Plan.UscPlan.Large.emoji),
    size = WidthOrWeight.Width(40.dp),
    tooltip = "Plan: ${Plan.UscPlan.entries.joinToString(" ") { "${it.emoji} ${it.apiString}" }}",
    renderer = CellRenderer.TextRenderer(
        textAlign = TextAlign.Center,
        sortExtractor = { it.plan.id },
        valueExtractor = { it.plan.emoji },
    )
)

fun <T : HasCategory> CategoryColumn() = TableColumn<T>(
    VisualIndicator.StringIndicator("Category"),
    WidthOrWeight.Width(80.dp),
    CellRenderer.TextRenderer { it.category.nameAndMaybeEmoji })

fun <T : HasVenue> VenueColumn() = TableColumn<T>(
    VisualIndicator.StringIndicator("Venue"),
    WidthOrWeight.Weight(0.4f),
    sortValueExtractor = { it.venue.name },
    renderer = CellRenderer.CustomRenderer { activity, col ->
        TableTextCell(
            text = activity.venue.name,
            size = col.size,
            textDecoration = if (activity.venue.isDeleted) TextDecoration.LineThrough else null,
        )
    },
)
