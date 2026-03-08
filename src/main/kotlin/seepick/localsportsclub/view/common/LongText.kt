package seepick.localsportsclub.view.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import seepick.localsportsclub.Lsc

@Composable
fun LongText(
    label: String? = null,
    text: String,
    onShowLongText: ((String) -> Unit)? = null,
    maxLines: Int = 2,
    tooltip: String = "Click to show full text",
) {
    var isOverflowing by remember { mutableStateOf(false) }
    val isClickable = onShowLongText != null && isOverflowing
    val hoverInteractionSource = remember { MutableInteractionSource() }
    val isHovered by hoverInteractionSource.collectIsHoveredAsState()
    val hoverModifier =
        if (isClickable && isHovered) {
            val baseColor = if (Lsc.isDarkTheme) Color.White else Color.Black
            Modifier.background(baseColor.copy(alpha = 0.06f))
        } else Modifier

    Row {
        if (label != null) {
            Text("$label: ", fontWeight = FontWeight.Bold)
        }
        Tooltip(tooltip) {
            Text(
                text = text, maxLines = maxLines, overflow = TextOverflow.Ellipsis, onTextLayout = {
                    isOverflowing = it.hasVisualOverflow
                }, modifier = Modifier.clickable(
                    enabled = isClickable,
                    interactionSource = hoverInteractionSource,
                    indication = null,
                ) {
                    onShowLongText!!(text)
                }
//                    .hoverable(hoverInteractionSource)
                    .then(hoverModifier))
        }
    }
}
