package seepick.localsportsclub.view.shared

import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.view.LscIcons
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.TableColumn

fun <T : HasVenue> CheckedinColumn() =
    TableColumn<T>(LscIcons.checkedin, ColSize.Width(40.dp), CellRenderer.TextRenderer {
        it.venue.activities.filter { it.state == ActivityState.Checkedin }.size + it.venue.freetrainings.filter { it.state == FreetrainingState.Checkedin }.size
    })

fun <T : HasVenue> RatingColumn() =
    TableColumn<T>("Rating", ColSize.Width(90.dp), CellRenderer.TextRenderer { it.venue.rating.string })
