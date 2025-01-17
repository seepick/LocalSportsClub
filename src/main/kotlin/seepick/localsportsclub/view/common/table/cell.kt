package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.view.common.WidthOrWeight

data class TableColumn<T>(
    val headerLabel: String? = null,
    val size: WidthOrWeight,
    val renderer: CellRenderer<T>,
    val sortingEnabled: Boolean = true,
    var sortValueExtractor: ((T) -> Any?)? = null,
) {
    init {
        if (sortValueExtractor == null) {
            when (renderer) {
                is CellRenderer.CustomRenderer -> if (sortingEnabled) error("No sort value extractor defined and not a TextRenderer!")
                is CellRenderer.TextRenderer -> sortValueExtractor = renderer.sortExtractor
            }
        }
    }
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun RowScope.TableHeader(
    text: String, size: WidthOrWeight, isSortEnabled: Boolean, isSortActive: Boolean, onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val bgColor = if (isSortActive) Lsc.colors.itemSelectedBg
    else if (isHovered && isSortEnabled) Lsc.colors.itemHoverBg
    else MaterialTheme.colors.surface
    TableCell(text = text,
        size = size,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .background(bgColor)
            .padding(top = 5.dp, bottom = 5.dp) // after bg color!
            .let {
                if (isSortActive || !isSortEnabled) it else {
                    it.onClick { onClick() }
                }
            })
}

fun RowScope.applyColSize(mod: Modifier, size: WidthOrWeight) = mod.let {
    when (size) {
        is WidthOrWeight.Weight -> mod.weight(size.value, true)
        is WidthOrWeight.Width -> mod.width(size.value)
    }
}

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
