package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class TableColumn<T>(
    val headerLabel: String,
    val size: ColSize,
    val renderer: CellRenderer<T>,
    val sortingEnabled: Boolean = true,
    var valueExtractor: ((T) -> Comparable<Any>)? = null,
) {
    init {
        if (valueExtractor == null) {
            @Suppress("UNCHECKED_CAST")
            when (renderer) {
                is CellRenderer.CustomRenderer -> if (sortingEnabled) error("No value extract defined and not a TextRenderer!")
                is CellRenderer.TextRenderer -> valueExtractor = renderer.extractor as (T) -> Comparable<Any>
            }
        }
    }
}

sealed interface CellRenderer<T> {
    data class TextRenderer<T>(val extractor: (T) -> Any) : CellRenderer<T>
    data class CustomRenderer<T>(val invoke: @Composable RowScope.(T, ColSize) -> Unit) : CellRenderer<T>
}

sealed interface ColSize {
    data class Weight(val value: Float) : ColSize
    data class Width(val value: Dp) : ColSize
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
    TableCell(text, size, modifier = Modifier
        .onPointerEvent(PointerEventType.Enter) { isHovered = true }
        .onPointerEvent(PointerEventType.Exit) { isHovered = false }
        .background(color = if (isHovered && !isSortActive && isSortEnabled) Color.LightGray else MaterialTheme.colors.background)
        .let {
            if (isSortActive || !isSortEnabled) it else {
                it.onClick { onClick() }
            }
        }
        .let { if (isSortActive) it.background(Color.Green) else it }
    )
}

@Composable
fun RowScope.TableData(text: String, size: ColSize) {
    TableCell(text, size)
}

fun RowScope.applyColSize(mod: Modifier, size: ColSize) = mod.let {
    when (size) {
        is ColSize.Weight -> mod.weight(size.value, true)
        is ColSize.Width -> mod.width(size.value)
    }
}

@Composable
fun RowScope.TableCell(text: String, size: ColSize, modifier: Modifier = Modifier) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .border(1.dp, Color.Black)
            .padding(8.dp)
            .let { applyColSize(it, size) }
            .then(modifier)
    )
}
