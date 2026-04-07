package seepick.localsportsclub.view.common.table

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.adjustHSB
import seepick.localsportsclub.view.common.brighter

fun rowBgColor(
    index: Int,
    isHovered: Boolean,
    isSelected: Boolean,
    isClickable: Boolean,
    primaryColor: Color? = null,
): Color {
    val evenOddColor = evenOddBgColor(index)
    val color = if (isSelected && isHovered) {
        Lsc.colors.itemHoverBg
    } else if (isSelected) {
        Lsc.colors.clickableSelected.let {
            if (Lsc.isDarkTheme) it.adjustHSB(addSaturation = 0.1f, addBrightness = 0.0f)
            else it.brighter(0.3f)
        }
    } else if (isClickable && isHovered) {
        val additionSat = if (Lsc.isDarkTheme) -0.2f else 1.0f
        val additionBright = if (Lsc.isDarkTheme) -0.3f else 0.3f
        primaryColor?.adjustHSB(addSaturation = additionSat, addBrightness = additionBright) ?: Lsc.colors.itemHoverBg
    } else {
        primaryColor?.copy(0.4f)?.compositeOver(evenOddColor) ?: evenOddColor
    }
    return color.compositeOver(evenOddColor)
}

fun evenOddBgColor(index: Int): Color = if (index % 2 == 0) Lsc.colors.backgroundVariant
else Lsc.colors.background
