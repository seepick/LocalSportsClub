package seepick.localsportsclub.view.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.search.SearchOption
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.applyTestTag
import seepick.localsportsclub.view.common.darker


@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchOption<*>.ClickableSearchText(
    testTag: String? = null,
) {
    var isHovered by remember { mutableStateOf(false) }
    val alpha = if (enabled) 1.0f else if (isHovered) 0.6f else 0.3f
    val color = if (isHovered) Lsc.colors.primary.darker()
    else if (enabled) Lsc.colors.primary
    else Lsc.colors.primaryBrighter

    Row(
        modifier = Modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .onClick { updateEnabled(!enabled) }
    ) {
        if (visualIndicator is VisualIndicator.VectorIndicator) {
            Icon(
                visualIndicator.icon, null,
                modifier = Modifier.alpha(alpha).height(17.dp)
            )
        }
        if (visualIndicator is VisualIndicator.BitmapIndicator) {
            Image(
                bitmap = visualIndicator.icon,
                contentDescription = null,
                alpha = alpha,
                modifier = Modifier.height(17.dp).padding(end = 2.dp)
            )
        }
        if (visualIndicator is VisualIndicator.EmojiIndicator) {
            Text(
                visualIndicator.emoji,
                modifier = Modifier
                    .alpha(alpha)
                    .padding(end = 2.dp)
            )
        }
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = color,
            modifier = Modifier
                .applyTestTag(testTag)
        )
        Spacer(modifier = Modifier.width(5.dp))
    }
}
