package seepick.localsportsclub.view.venue.detail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.roundedCornerMask
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun String.pluralS(number: Int) = if (number == 1) this else "${this}s"

private val monthFormat = DateTimeFormatter.ofPattern("MMMM")

@Preview
@Composable
private fun MonthlyVisitsPanel(
) {
    Column {
        val modifier = Modifier.size(200.dp, 20.dp)
        fun model(checkins: Int = 0, booked: Int = 0) = MonthlyVisitsModel(
            checkins = checkins,
            booked = booked,
            maxVisits = 6,
        )
        MonthlyVisitsPanel(model(), modifier)
        MonthlyVisitsPanel(model(checkins = 1), modifier)
        MonthlyVisitsPanel(model(booked = 2), modifier)
        MonthlyVisitsPanel(model(checkins = 1, booked = 2), modifier)
        MonthlyVisitsPanel(model(checkins = 4), modifier)
        MonthlyVisitsPanel(model(checkins = 5), modifier)
        MonthlyVisitsPanel(model(checkins = 6), modifier)
    }
}

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
        "${model.available} ${"visit".pluralS(model.available)} available " + "in ${monthFormat.format(LocalDate.now())} " + "(${model.checkins} checkins, ${model.booked} booked)"
    ) {
        Box(
            modifier = modifier
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize().padding(top = 4.dp, bottom = 4.dp)
            ) {
                val width = size.width
                val height = size.height
                val checkinsWidth = (width * percentageCheckins).toFloat()
                val bookedWidth = (width * percentageBooked).toFloat()
                val availableWidth = (width * percentageAvailable).toFloat()
                val availableX = checkinsWidth + bookedWidth
                roundedCornerMask(width, height) {
                    drawRect(
                        topLeft = Offset(0.0f, 0.0f),
                        color = Lsc.colors.checkins,
                        size = Size(checkinsWidth, height),
                    )
                    drawRect(
                        topLeft = Offset(checkinsWidth, 0.0f),
                        color = Lsc.colors.booked,
                        size = Size(bookedWidth, height),
                    )
                    drawRect(
                        topLeft = Offset(availableX, 0.0f),
                        color = Lsc.colors.available,
                        size = Size(availableWidth, height),
                    )
                }
                drawText(
                    textMeasurer,
                    text = "${model.available}/${model.maxVisits}",
                    topLeft = Offset(minOf(availableX + 5.0f, width - 25.0f), 0.0f),
                    style = TextStyle(color = Color.Black, fontSize = 10.sp),
                )
            }
        }
    }
}

data class MonthlyVisitsModel(
    val checkins: Int,
    val booked: Int,
    val maxVisits: Int,
) {
    val used = checkins + booked
    val available = maxVisits - used
}

fun Venue.toMonthlyVisitsModel(today: LocalDate): MonthlyVisitsModel {
    val currentMonthsActivities = activities.filter {
        it.dateTimeRange.from.month == today.month && it.dateTimeRange.from.year == today.year
    }
    return MonthlyVisitsModel(
        checkins = currentMonthsActivities.count { it.state == ActivityState.Checkedin },
        booked = currentMonthsActivities.count { it.state == ActivityState.Booked },
        maxVisits = 6, // TODO venue.visitLimitsForCurrentPlan (pre-calc in data storage)
    )
}
