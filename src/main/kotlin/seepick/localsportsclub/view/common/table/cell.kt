package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc

data class TableColumn<T>(
    val headerLabel: String? = null,
    val size: ColSize,
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
    data class TextRenderer<T>(val extractor: (T) -> Any, val sortExtractor: (T) -> Any?) : CellRenderer<T> {
        companion object {
            operator fun <T> invoke(extractor: (T) -> Any): TextRenderer<T> =
                TextRenderer(extractor, extractor)
        }
    }

    data class CustomRenderer<T>(val invoke: @Composable RowScope.(T, TableColumn<T>) -> Unit) : CellRenderer<T>
}

sealed interface ColSize {
    data class Weight(val value: Float) : ColSize
    data class Width(val value: Dp) : ColSize
}

fun RowScope.ModifierWith(colSize: ColSize) = Modifier.let {
    when (colSize) {
        is ColSize.Weight -> it.weight(colSize.value)
        is ColSize.Width -> it.width(colSize.value)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun RowScope.TableHeader(
    text: String,
    size: ColSize,
    isSortEnabled: Boolean,
    isSortActive: Boolean,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val bgColor =
        if (isSortActive) Lsc.colors.itemSelectedBg
        else if (isHovered && isSortEnabled) Lsc.colors.itemHoverBg
        else MaterialTheme.colors.surface
    TableCell(
        text = text,
        size = size,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .defaultMinSize(0.dp, 25.dp)
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .background(bgColor)
            .let {
                if (isSortActive || !isSortEnabled) it else {
                    it.onClick { onClick() }
                }
            }
    )
}

fun RowScope.applyColSize(mod: Modifier, size: ColSize) = mod.let {
    when (size) {
        is ColSize.Weight -> mod.weight(size.value, true)
        is ColSize.Width -> mod.width(size.value)
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    size: ColSize,
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
        modifier = Modifier
            .let { applyColSize(it, size) }
            .align(Alignment.CenterVertically)
            .then(modifier)
    )
}
