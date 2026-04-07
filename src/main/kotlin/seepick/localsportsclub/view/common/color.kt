package seepick.localsportsclub.view.common

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Color.adjustHSB(addSaturation: Float = 0.1f, addBrightness: Float = 0.1f): Color {
    val (hue, saturation, brightness) = java.awt.Color.RGBtoHSB(
        (this.red * 255).toInt(),
        (this.green * 255).toInt(),
        (this.blue * 255).toInt(),
        null
    )
    return Color.hsv(
        hue = hue * 360f,
        saturation = (saturation + addSaturation).coerceIn(0.0f..1.0f),
        value = (brightness + addBrightness).coerceIn(0.0f..1.0f),
    )
}

// maybe simple use AWT's Color to do this
fun Color.darker(amount: Float = 0.2f): Color =
    copy(alpha = alpha, red = red - amount, green = green - amount, blue = blue - amount)

fun Color.brighter(amount: Float = 0.2f): Color =
    copy(alpha = alpha, red = red + amount, green = green + amount, blue = blue + amount)

sealed interface ColorOrBrush {
    data class ColorOr(val color: Color) : ColorOrBrush
    data class BrushOr(val brush: Brush) : ColorOrBrush
}

fun Modifier.background(colorOrBrush: ColorOrBrush): Modifier =
    when (colorOrBrush) {
        is ColorOrBrush.BrushOr -> background(colorOrBrush.brush)
        is ColorOrBrush.ColorOr -> background(colorOrBrush.color)
    }
