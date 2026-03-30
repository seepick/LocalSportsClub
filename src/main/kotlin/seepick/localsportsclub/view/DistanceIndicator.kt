package seepick.localsportsclub.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import seepick.localsportsclub.service.model.HasDistance

@Composable
@Preview
fun DistanceIndicatorPreview() {
    androidx.compose.foundation.layout.Column {
        repeat(15) { x ->
            Box(Modifier.size(60.dp, 45.dp).padding(10.dp)) {
                DistanceIndicator(object : HasDistance {
                    override val distanceInKm = x * 0.4
                })
            }
        }
    }
}


@Composable
fun DistanceIndicator(item: HasDistance) {
    val textMeasurer = rememberTextMeasurer()
    val maxKm = HasDistance.MAX_DISTANCE_KM
    val distance = item.distanceInKm.coerceAtMost(maxKm)
    val percentageWidth = distance / maxKm

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        drawRect(
            topLeft = Offset(0.0f, 0.0f),
            color = Lsc.colors.forDistance(percentageWidth),
            size = Size((width.toDouble() * percentageWidth).toFloat(), height),
        )
        drawRect(
            // border
            topLeft = Offset(0.0f, 0.0f),
            color = Lsc.colors.onSurfaceAlphaLow,
            style = Stroke(width = 1.0f),
            size = Size(width, height),
        )
        val textSize = textMeasurer.measure(item.distanceFormatted)
        drawText(
            text = item.distanceFormatted,
            textMeasurer = textMeasurer,
            topLeft = Offset(
                x = width / 2.0f - textSize.size.width / 2.0f,
                y = height / 2.0f - textSize.size.height / 2.0f
            ),
            style = TextStyle(color = Lsc.colors.onBackground, fontSize = 12.sp),
        )
    }
}
