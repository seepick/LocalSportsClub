package seepick.localsportsclub.usage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyShortPrint
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.darker
import seepick.localsportsclub.view.common.roundedCornerMask
import kotlin.math.abs

private val colorError = Color.Red

@Composable
fun UsageView(
    usageStorage: UsageStorage = koinInject(),
    clock: Clock = koinInject(),
) {
    if (!usageStorage.isUsageVisible) {
        return
    }
    var showStatsDialog by remember { mutableStateOf(false) }

    if (showStatsDialog) {
        UsageStatsDialog(
            onClose = { showStatsDialog = false }
        )
    }
    val checkedinCount by usageStorage.checkedinCount.collectAsState(0)
    val bookedCount by usageStorage.reservedCount.collectAsState(0)
    val percentageCheckedin by usageStorage.percentageCheckedin.collectAsState(0.0)
    val percentageBooked by usageStorage.percentageBooked.collectAsState(0.0)
    val year = clock.today().year
    val periodColor = calcPeriodColor(abs(usageStorage.percentagePeriod - (percentageCheckedin + percentageBooked)))
    Row {
        Column {
            Row {
                Text("Period: ")
                Text(
                    "${usageStorage.periodFirstDay.prettyShortPrint(year)}-${
                        usageStorage.periodLastDay.prettyShortPrint(year)
                    }", color = periodColor.darker()
                )
            }
            Row {
                Text(buildAnnotatedString {
                    append("Usage: ")
                    withStyle(style = SpanStyle(color = Lsc.colors.checkins, fontWeight = FontWeight.Bold)) {
                        append(checkedinCount.toString())
                    }
                    append("+")
                    withStyle(style = SpanStyle(color = Lsc.colors.booked, fontWeight = FontWeight.Bold)) {
                        append(bookedCount.toString())
                    }
                    append(" / ${usageStorage.maxBookingsForPeriod}")
                })
            }
            UsageIndicator(
                percentagePeriod = usageStorage.percentagePeriod,
                percentageCheckedin = percentageCheckedin,
                percentageBooked = percentageBooked,
                periodColor = periodColor,
            )
        }
        Tooltip("Show usage statistics") {
            TextButton(
                onClick = { showStatsDialog = true },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .padding(0.dp)
//                .background(Color.Red)
            ) {
                Icon(Icons.Default.Info, null, modifier = Modifier.padding(0.dp))
            }
        }
    }
}

@Preview
@Composable
fun UsageIndicatorPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UsageIndicator(percentagePeriod = 0.0, percentageCheckedin = 0.0, percentageBooked = 0.0)
        UsageIndicator(percentagePeriod = 0.2, percentageCheckedin = 0.0, percentageBooked = 0.0)
        UsageIndicator(percentagePeriod = 0.5, percentageCheckedin = 0.0, percentageBooked = 0.0)
        UsageIndicator(percentagePeriod = 0.8, percentageCheckedin = 0.0, percentageBooked = 0.0)
        UsageIndicator(percentagePeriod = 1.0, percentageCheckedin = 0.0, percentageBooked = 0.0)
        UsageIndicator(percentagePeriod = 0.1, percentageCheckedin = 0.6, percentageBooked = 0.0)
        UsageIndicator(percentagePeriod = 0.1, percentageCheckedin = 0.0, percentageBooked = 0.6)
        UsageIndicator(percentagePeriod = 0.1, percentageCheckedin = 0.1, percentageBooked = 0.1)
        UsageIndicator(percentagePeriod = 0.1, percentageCheckedin = 0.4, percentageBooked = 0.5)
        UsageIndicator(percentagePeriod = 0.4, percentageCheckedin = 0.4, percentageBooked = 0.5)
        UsageIndicator(percentagePeriod = 0.9, percentageCheckedin = 0.4, percentageBooked = 0.5)
        UsageIndicator(percentagePeriod = 1.0, percentageCheckedin = 0.4, percentageBooked = 0.5)
        UsageIndicator(percentagePeriod = 1.0, percentageCheckedin = 0.4, percentageBooked = 0.6)
    }
}

private fun calcPeriodColor(distance: Double): Color {
    val d = distance.coerceIn(0.0, 1.0).toFloat()
    // 0.0 \=\> green (120°), 0.5 \=\> orange (30°), 1.0 \=\> red (0°)
    val hue = 120f * (1f - d)
    return Color.hsv(hue, 1f, 1f)
}

@Composable
fun UsageIndicator(
    percentagePeriod: Double,
    percentageCheckedin: Double,
    percentageBooked: Double,
    periodColor: Color = calcPeriodColor(abs(percentagePeriod - (percentageCheckedin + percentageBooked))),
) {
    Canvas(modifier = Modifier.size(200.dp, 15.dp)) {
        val width = size.width
        val height = size.height
        val periodWidth = (width * percentagePeriod).toFloat()
        val checkedinWidth = (width * percentageCheckedin).toFloat()
        val bookedWidth = (width * percentageBooked).toFloat()
        roundedCornerMask(width, height) {
            drawRect(
                topLeft = Offset.Zero,
                color = Lsc.colors.backgroundGray,
                size = Size(width, height),
            )
            drawRect(
                topLeft = Offset.Zero,
                color = periodColor,
                size = Size(periodWidth, height / 2),
            )
            if (percentageCheckedin + percentageBooked > 1.0) {
                drawRect(
                    topLeft = Offset(0.0f, height / 2),
                    color = colorError,
                    size = Size(width, height / 2),
                )
            } else {
                drawRect(
                    topLeft = Offset(0.0f, height / 2),
                    color = Lsc.colors.checkins,
                    size = Size(checkedinWidth, height / 2),
                )
                drawRect(
                    topLeft = Offset(checkedinWidth, height / 2),
                    color = Lsc.colors.booked,
                    size = Size(bookedWidth, height / 2),
                )
            }
        }
    }
}
