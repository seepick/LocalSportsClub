package seepick.localsportsclub.view.common

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import seepick.localsportsclub.Lsc
import kotlin.math.max
import kotlin.math.min

fun rowBgColor(
    index: Int,
    isHovered: Boolean,
    isSelected: Boolean,
    isClickable: Boolean,
    primaryColor: Color? = null
): Color {
    val alternateColor = alternateBgColor(index)
    val selectedColor = primaryColor?.adjustHSB() ?: Lsc.colors.itemSelectedBg
    val hoverColor = primaryColor?.brighter() ?: Lsc.colors.itemHoverBg
    return (
            if (isSelected) selectedColor.copy(0.8f)
            else if (isClickable && isHovered) hoverColor.copy(0.4f)
            else alternateBgColor(index).let {
                primaryColor?.copy(0.2f)?.compositeOver(it) ?: it
            }
            ).compositeOver(alternateColor)
}

private fun alternateBgColor(index: Int): Color =
    if (index % 2 == 0) Lsc.colors.backgroundVariant
    else Lsc.colors.background

private fun Color.adjustHSB(): Color {
    val (hue, saturation, brightness) = java.awt.Color.RGBtoHSB(
        (this.red * 255).toInt(),
        (this.green * 255).toInt(),
        (this.blue * 255).toInt(),
        null
    )
    return Color.hsv(
        hue = hue * 360f,
        saturation = min(saturation + 0.1f, 1f),
        value = max(0f, min(brightness + 0.1f, 1f)),
    )
}

// maybe simple use AWT's Color to do this
fun Color.darker(): Color = copy(alpha = alpha, red = red - 0.2f, green = green - 0.2f, blue = blue - 0.2f)
fun Color.brighter(): Color = copy(alpha = alpha, red = red + 0.2f, green = green + 0.2f, blue = blue + 0.2f)

sealed interface ColorOrBrush {
    data class ColorOr(val color: Color) : ColorOrBrush
    data class BrushOr(val brush: Brush) : ColorOrBrush
}

fun Modifier.background(colorOrBrush: ColorOrBrush): Modifier =
    when (colorOrBrush) {
        is ColorOrBrush.BrushOr -> background(colorOrBrush.brush)
        is ColorOrBrush.ColorOr -> background(colorOrBrush.color)
    }
