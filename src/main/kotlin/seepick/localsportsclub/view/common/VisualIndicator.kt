package seepick.localsportsclub.view.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed interface VisualIndicator {
    data object NoIndicator : VisualIndicator
    data class EmojiIndicator(val emoji: String) : VisualIndicator
    data class StringIndicator(val label: String) : VisualIndicator
    data class VectorIndicator(val icon: ImageVector) : VisualIndicator
    data class BitmapIndicator(val icon: ImageBitmap) : VisualIndicator
}

@Composable
fun VisualIndicator.composeIt(
    alpha: Float,
    paddingEnd: Dp? = 2.dp,
) {
    val modifier2 = Modifier.let {
        if (paddingEnd != null) it.padding(end = paddingEnd) else it
    }
    when (this) {
        is VisualIndicator.BitmapIndicator -> Image(
            bitmap = this.icon,
            contentDescription = null, alpha = alpha,
            modifier = Modifier.height(17.dp).then(modifier2)
        )

        is VisualIndicator.VectorIndicator -> Icon(
            this.icon, null,
            modifier = Modifier.alpha(alpha).height(17.dp)
        )

        is VisualIndicator.StringIndicator -> Text(
            this.label,
            modifier = Modifier.alpha(alpha).then(modifier2)
        )

        is VisualIndicator.EmojiIndicator -> Text(
            this.emoji,
            modifier = Modifier.alpha(alpha)
                .then(modifier2)
        )

        VisualIndicator.NoIndicator -> { /*nothing*/
        }
    }
}
