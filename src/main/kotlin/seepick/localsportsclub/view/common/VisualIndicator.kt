package seepick.localsportsclub.view.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface VisualIndicator {
    data object NoIndicator : VisualIndicator
    data class EmojiIndicator(val emoji: String) : VisualIndicator
    data class VectorIndicator(val icon: ImageVector) : VisualIndicator
    data class BitmapIndicator(val icon: ImageBitmap) : VisualIndicator
}
