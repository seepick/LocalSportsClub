package seepick.localsportsclub.view.usage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyShortPrint
import seepick.localsportsclub.view.StatsDialog

private val colorBg = Color(0xFF9B9B9B)
private val colorPeriod = Color(0xFFFF9300)
private val colorBooked = Color(0xFF48931C)
private val colorCheckedin = Color(0xFF4861DE)
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
        StatsDialog(
            onClose = { showStatsDialog = false }
        )
    }
    val checkedinCount by usageStorage.checkedinCount.collectAsState(0)
    val bookedCount by usageStorage.reservedCount.collectAsState(0)
    val percentageCheckedin by usageStorage.percentageCheckedin.collectAsState(0.0)
    val percentageBooked by usageStorage.percentageBooked.collectAsState(0.0)
    val year = clock.today().year
    Row {
        Column {
            Row {
                Text("Period: ")
                Text(
                    "${usageStorage.periodFirstDay.prettyShortPrint(year)}-${
                        usageStorage.periodLastDay.prettyShortPrint(year)
                    }", color = colorPeriod
                )
            }
            Row {
                Text(buildAnnotatedString {
                    append("Usage: ")
                    withStyle(style = SpanStyle(color = colorCheckedin, fontWeight = FontWeight.Bold)) {
                        append(checkedinCount.toString())
                    }
                    append("+")
                    withStyle(style = SpanStyle(color = colorBooked, fontWeight = FontWeight.Bold)) {
                        append(bookedCount.toString())
                    }
                    append(" / ${usageStorage.maxBookingsForPeriod}")
                })
            }
            UsageIndicator(
                percentagePeriod = usageStorage.percentagePeriod,
                percentageCheckedin = percentageCheckedin,
                percentageBooked = percentageBooked,
            )
        }
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

@Composable
fun UsageIndicator(
    percentagePeriod: Double,
    percentageCheckedin: Double,
    percentageBooked: Double,
) {
    Canvas(modifier = Modifier.size(200.dp, 10.dp).background(colorBg)) {
        val width = size.width
        val height = size.height
        val periodWidth = (width * percentagePeriod).toFloat()
        val checkedinWidth = (width * percentageCheckedin).toFloat()
        val bookedWidth = (width * percentageBooked).toFloat()
        drawRect(
            topLeft = Offset.Zero,
            color = colorPeriod,
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
                color = colorCheckedin,
                size = Size(checkedinWidth, height / 2),
            )
            drawRect(
                topLeft = Offset(checkedinWidth, height / 2),
                color = colorBooked,
                size = Size(bookedWidth, height / 2),
            )
        }
    }
}

@Preview
@Composable
fun UsageIndicator_Preview() {
    UsageIndicator(
        percentagePeriod = 0.65,
        percentageCheckedin = 0.6,
        percentageBooked = 0.1,
    )
}
