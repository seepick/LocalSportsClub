package seepick.localsportsclub.view.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import seepick.localsportsclub.Lsc

@Composable
fun LongText(
    label: String? = null,
    text: String,
    onShowLongText: ((String) -> Unit)? = null,
    maxLines: Int? = null,
    tooltip: String = "Click to show full text",
) {
    var isOverflowing by remember { mutableStateOf(false) }
    val isClickable = onShowLongText != null && isOverflowing
    val hoverInteractionSource = remember { MutableInteractionSource() }
    val isHovered by hoverInteractionSource.collectIsHoveredAsState()
    val hoverModifier = if (isClickable && isHovered) {
        val baseColor = if (Lsc.isDarkTheme) Color.White else Color.Black
        Modifier.background(baseColor.copy(alpha = 0.06f))
    } else Modifier
    var boxHeight by remember { mutableStateOf(0f) }

    Row(modifier = Modifier.let { if (maxLines == null) it.fillMaxHeight() else it }) {
        if (label != null) {
            Text("$label: ", fontWeight = FontWeight.Bold)
        }
        Tooltip(tooltip) {
            Box(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    boxHeight = coordinates.size.height.toFloat()
                }
            ) {
                Text(
                    text = text,
                    maxLines = maxLines ?: Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { isOverflowing = it.hasVisualOverflow },
                    modifier = Modifier.clickable(
                        enabled = isClickable,
                        interactionSource = hoverInteractionSource,
                        indication = null,
                        onClick = { onShowLongText!!(text) },
                    ).then(hoverModifier)
                )
                if (isOverflowing && maxLines != 1) {
                    val gradientColor = Lsc.colors.background
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(gradientColor.copy(alpha = 0f), gradientColor.copy(alpha = 1f)),
                                    startY = boxHeight * 0.7f, // where gradient should start in percentage
                                )
                            )
                    )
                }
            }
        }
    }
}
