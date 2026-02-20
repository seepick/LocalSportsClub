package seepick.localsportsclub.view.shared

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.seepick.uscclient.plan.Plan
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.HasDistance
import seepick.localsportsclub.service.model.HasPlan
import seepick.localsportsclub.service.model.HasVenue
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.TableColumn

fun <T : HasVenue> CheckedinColumn(paddingRight: Boolean = false) = TableColumn<T>(
    header = VisualIndicator.EmojiIndicator(LscIcons.checkedinEmoji),
    size = WidthOrWeight.Width(40.dp),
    renderer = CellRenderer.TextRenderer(textAlign = TextAlign.Right, paddingRight = paddingRight) {
        it.venue.activities.filter { it.state == ActivityState.Checkedin }.size + it.venue.freetrainings.filter { it.state == FreetrainingState.Checkedin }.size
    },
    tooltip = "Check-ins"
)

fun <T : HasVenue> RatingColumn() =
    TableColumn<T>(
        VisualIndicator.StringIndicator("Rating"),
        WidthOrWeight.Width(80.dp),
        CellRenderer.TextRenderer { it.venue.rating.label })

fun <T : HasDistance> DistanceColumn() =
    TableColumn<T>(
        VisualIndicator.StringIndicator("km"),
        WidthOrWeight.Width(35.dp),
        CellRenderer.TextRenderer(textAlign = TextAlign.Right) { it.distanceInKm }
    )

fun <T : HasPlan> PlanColumn() =
    TableColumn<T>(
        header = VisualIndicator.StringIndicator(Plan.UscPlan.Large.emoji),
        size = WidthOrWeight.Width(40.dp),
        tooltip = "Plan: ${Plan.UscPlan.entries.joinToString(" ") { "${it.emoji} ${it.apiString}" }}",
        renderer = CellRenderer.TextRenderer(
            textAlign = TextAlign.Center,
            sortExtractor = { it.plan.id },
            valueExtractor = { it.plan.emoji },
        )
    )
