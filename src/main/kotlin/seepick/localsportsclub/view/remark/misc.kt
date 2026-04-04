package seepick.localsportsclub.view.remark

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString.Builder
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import seepick.localsportsclub.service.model.RemarkRating

inline fun <R : Any> Builder.withColor(
    rating: RemarkRating?,
    block: Builder.() -> R,
): R = withStyle(SpanStyle(color = rating?.color ?: Color.Unspecified), block)
