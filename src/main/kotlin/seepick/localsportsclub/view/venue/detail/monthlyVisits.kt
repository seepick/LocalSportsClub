package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.view.common.Tooltip
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val colorCheckins = Color(0xFFFF9300)
private val colorBooked = Color(0xFF9B9B9B)
private val colorAvailable = Color(0xFF48931C)

fun String.pluralS(number: Int) = if (number == 1) this else "${this}s"

private val monthFormat = DateTimeFormatter.ofPattern("MMMM")

@Composable
fun MonthlyVisitsPanel(
    model: MonthlyVisitsModel,
    modifier: Modifier = Modifier,
) {
    val percentageCheckins = model.checkins.toDouble() / model.maxVisits
    val percentageBooked = model.booked.toDouble() / model.maxVisits
    val percentageAvailable = model.available.toDouble() / model.maxVisits
    val textMeasurer = rememberTextMeasurer()
    Tooltip(
        "${model.available} ${"visit".pluralS(model.available)} available " +
                "in ${monthFormat.format(LocalDate.now())} " +
                "(${model.checkins} checkins, ${model.booked} booked)"
    ) {
        Box(
            modifier = Modifier
//                .size(140.dp, 20.dp)
                .then(modifier)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize().padding(top = 4.dp, bottom = 4.dp)
            ) {
                val width = size.width
                val height = size.height
                val checkinsWidth = (width * percentageCheckins).toFloat()
                val bookedWidth = (width * percentageBooked).toFloat()
                val availableWidth = (width * percentageAvailable).toFloat()
                drawRect(
                    topLeft = Offset(0.0f, 0.0f),
                    color = colorCheckins,
                    size = Size(checkinsWidth, height),
                )
                drawRect(
                    topLeft = Offset(checkinsWidth, 0.0f),
                    color = colorBooked,
                    size = Size(bookedWidth, height),
                )
                val availableX = checkinsWidth + bookedWidth
                drawRect(
                    topLeft = Offset(availableX, 0.0f),
                    color = colorAvailable,
                    size = Size(availableWidth, height),
                )
                drawText(
                    textMeasurer,
                    "${model.available}/${model.maxVisits}",
                    topLeft = Offset(minOf(availableX + 8.0f, width - 40.0f), 0.0f),
                    style = TextStyle(color = Lsc.colors.onPrimary, fontSize = 10.sp),
                )
            }
        }
    }
}

data class MonthlyVisitsModel(
    val checkins: Int,
    val booked: Int,
    val maxVisits: Int = 6, // needs to be adjusted according to plan
) {
    val used = checkins + booked
    val available = maxVisits - used
}

fun List<Activity>.toMonthlyVisitsModel(today: LocalDate): MonthlyVisitsModel {
    val currentMonthsActivities =
        filter { it.dateTimeRange.from.month == today.month && it.dateTimeRange.from.year == today.year }
    return MonthlyVisitsModel(
        checkins = currentMonthsActivities.count { it.state == ActivityState.Checkedin },
        booked = currentMonthsActivities.count { it.state == ActivityState.Booked },
    )
}
