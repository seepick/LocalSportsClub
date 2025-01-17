package seepick.localsportsclub.view.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

fun Modifier.widthOrFill(widthOrFill: WidthOrFill) = let {
    when (widthOrFill) {
        WidthOrFill.FillWidth -> it.fillMaxWidth(1f)
        is WidthOrFill.Width -> it.width(widthOrFill.width)
    }
}

sealed interface WidthOrFill {
    data object FillWidth : WidthOrFill
    data class Width(val width: Dp) : WidthOrFill
}

sealed interface WidthOrWeight {
    data class Weight(val value: Float) : WidthOrWeight
    data class Width(val value: Dp) : WidthOrWeight
}

@Suppress("FunctionName")
fun RowScope.ModifierWith(widthOrWeight: WidthOrWeight) = Modifier.let {
    when (widthOrWeight) {
        is WidthOrWeight.Weight -> it.weight(widthOrWeight.value)
        is WidthOrWeight.Width -> it.width(widthOrWeight.value)
    }
}

