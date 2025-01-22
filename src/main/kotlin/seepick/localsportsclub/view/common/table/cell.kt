package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import seepick.localsportsclub.view.common.WidthOrWeight

@Composable
fun RowScope.TableCell(
    text: String,
    size: WidthOrWeight,
    textDecoration: TextDecoration? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        textDecoration = textDecoration,
        maxLines = 1,
        textAlign = textAlign,
        fontWeight = fontWeight,
        overflow = TextOverflow.Ellipsis,
        modifier = applyColSize(Modifier, size)
            .align(Alignment.CenterVertically)
            .then(modifier)
    )
}

sealed interface CellRenderer<T> {
    data class TextRenderer<T>(
        val extractor: (T) -> Any,
        val sortExtractor: (T) -> Any?,
        val textAlign: TextAlign? = null,
        val paddingLeft: Boolean = false,
        val paddingRight: Boolean = false,
    ) : CellRenderer<T> {
        companion object {
            operator fun <T> invoke(
                textAlign: TextAlign? = null,
                paddingLeft: Boolean = false,
                paddingRight: Boolean = false,
                extractor: (T) -> Any,
            ): TextRenderer<T> = TextRenderer(
                extractor = extractor,
                sortExtractor = extractor,
                textAlign = textAlign,
                paddingLeft = paddingLeft,
                paddingRight = paddingRight,
            )
        }
    }

    data class CustomRenderer<T>(val invoke: @Composable RowScope.(T, TableColumn<T>) -> Unit) : CellRenderer<T>
}

fun RowScope.applyColSize(mod: Modifier, size: WidthOrWeight) = mod.let {
    when (size) {
        is WidthOrWeight.Weight -> mod.weight(size.value, true)
        is WidthOrWeight.Width -> mod.width(size.value)
    }
}
