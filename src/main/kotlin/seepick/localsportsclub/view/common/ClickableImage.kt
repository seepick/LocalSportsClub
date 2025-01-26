package seepick.localsportsclub.view.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import org.jetbrains.skia.impl.Stats.enabled

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun ClickableImage(
    icon: ImageBitmap,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var isHovered by remember { mutableStateOf(false) }
    val alpha = if (enabled) 1.0f else if (isHovered) 0.8f else 0.5f
    Image(
        bitmap = icon,
        contentDescription = null,
        modifier = modifier
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .onClick(onClick = onClick),
        alpha = alpha
    )
}
