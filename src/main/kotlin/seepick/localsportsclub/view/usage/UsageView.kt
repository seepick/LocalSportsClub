package seepick.localsportsclub.view.usage

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import seepick.localsportsclub.api.UscConfig
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyShortPrint

private val colorPeriod = Color(0xFFFF9300)
private val colorBooked = Color(0xFF48931C)
private val colorCheckedin = Color(0xFF4861DE)

@Composable
fun UsageView(
    usageStorage: UsageStorage = koinInject(),
    clock: Clock = koinInject(),
    uscConfig: UscConfig = koinInject(),
) {
    val checkedinCount by usageStorage.checkedinCount.collectAsState(0)
    val bookedCount by usageStorage.bookedCount.collectAsState(0)
    val percentageCheckedin by usageStorage.percentageCheckedin.collectAsState(0.0)
    val percentageBooked by usageStorage.percentageBooked.collectAsState(0.0)
    val year = clock.today().year
    val usageConfig = uscConfig.usageConfig
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
                append(" / ${usageConfig.maxBookingsForPeriod}")
            })
        }
        UsageIndicator(
            percentagePeriod = usageStorage.percentagePeriod,
            percentageCheckedin = percentageCheckedin,
            percentageBooked = percentageBooked,
        )
    }
}


private val colorBg = Color(0xFF9B9B9B)

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
            color = colorPeriod, size = Size(periodWidth, height / 2)
        )
        drawRect(
            topLeft = Offset(0.0f, height / 2), color = colorCheckedin, size = Size(checkedinWidth, height / 2)
        )
        drawRect(
            topLeft = Offset(checkedinWidth, height / 2), color = colorBooked, size = Size(bookedWidth, height / 2)
        )
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
