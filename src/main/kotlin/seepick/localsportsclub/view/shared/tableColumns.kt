package seepick.localsportsclub.view.shared

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.HasDistance
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
    tooltip = "check-ins"
)

fun <T : HasVenue> RatingColumn() =
    TableColumn<T>(
        VisualIndicator.StringIndicator("Rating"),
        WidthOrWeight.Width(90.dp),
        CellRenderer.TextRenderer { it.venue.rating.label })

fun <T : HasDistance> DistanceColumn() =
    TableColumn<T>(
        VisualIndicator.StringIndicator("km"),
        WidthOrWeight.Width(50.dp),
        CellRenderer.TextRenderer(textAlign = TextAlign.Right) { it.distanceInKm }
    )
