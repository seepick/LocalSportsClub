package seepick.localsportsclub.view.shared

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.HasVenue
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.TableColumn

fun <T : HasVenue> CheckedinColumn(paddingRight: Boolean = false) = TableColumn<T>(
    LscIcons.checkedin,
    WidthOrWeight.Width(40.dp),
    CellRenderer.TextRenderer(textAlign = TextAlign.Right, paddingRight = paddingRight) {
        it.venue.activities.filter { it.state == ActivityState.Checkedin }.size + it.venue.freetrainings.filter { it.state == FreetrainingState.Checkedin }.size
    })

fun <T : HasVenue> RatingColumn() =
    TableColumn<T>("Rating", WidthOrWeight.Width(90.dp), CellRenderer.TextRenderer { it.venue.rating.label })
