package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.onClick
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.SortDirection
import seepick.localsportsclub.view.common.ColorOrBrush
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.background
import seepick.localsportsclub.view.common.bottomBorder
import seepick.localsportsclub.view.common.brighter
import seepick.localsportsclub.view.common.composeIt
import seepick.localsportsclub.view.common.darker

@OptIn(ExperimentalFoundationApi::class)
fun <T> LazyListScope.renderTableHeader(
    columns: List<TableColumn<T>>,
    sortColumn: TableColumn<T>?,
    sortDirection: SortDirection,
    onHeaderClicked: (TableColumn<T>) -> Unit,
) {
    stickyHeader {
        Row(
            modifier = Modifier.height(30.dp)
        ) {
            columns.forEach { col ->
                TableHeader(
                    header = col.header,
                    size = col.size,
                    isSortEnabled = col.sortingEnabled,
                    isSortActive = col == sortColumn,
                    sortDirection = sortDirection,
                    onClick = { onHeaderClicked(col) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun RowScope.TableHeader(
    header: VisualIndicator,
    size: WidthOrWeight,
    isSortEnabled: Boolean,
    isSortActive: Boolean,
    sortDirection: SortDirection,
    onClick: () -> Unit,
) {
    var isHovered by remember { mutableStateOf(false) }
    val background: ColorOrBrush = tableHeaderBgColor(isSortEnabled, isSortActive, isHovered, sortDirection)
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = applyColSize(Modifier, size)
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .background(background)
            .bottomBorder(1.dp, Lsc.colors.primary)
            .padding(top = 5.dp, bottom = 5.dp) // after bg color!
            .fillMaxHeight(1f)
            .let {
                if (!isSortEnabled) it else {
                    it.onClick { onClick() }
                }
            }
    ) {
        when (header) {
            is VisualIndicator.BitmapIndicator, is VisualIndicator.VectorIndicator -> {
                header.composeIt(1.0f)
            }

            VisualIndicator.NoIndicator, is VisualIndicator.StringIndicator, is VisualIndicator.EmojiIndicator -> {
                val headerText = when (header) {
                    VisualIndicator.NoIndicator -> ""
                    is VisualIndicator.StringIndicator -> header.label
                    is VisualIndicator.EmojiIndicator -> header.emoji
                    is VisualIndicator.BitmapIndicator -> error("bitmap indicator not implemented, lol")
                    is VisualIndicator.VectorIndicator -> error("vector indicator not implemented, lol")
                }
                TableTextCell(
                    text = headerText,
                    size = size,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun tableHeaderBgColor(
    isSortEnabled: Boolean,
    isSortActive: Boolean,
    isHovered: Boolean,
    sortDirection: SortDirection,
): ColorOrBrush = if (isSortActive) {
    val gradient1 = if (isHovered) Lsc.colors.primaryBrighter.brighter() else Lsc.colors.primary.brighter()
    val gradient2 = if (isHovered) Lsc.colors.primaryBrighter else Lsc.colors.primary
    val gradient3 = if (isHovered) Lsc.colors.primaryBrighter.darker() else Lsc.colors.primary.darker()
    ColorOrBrush.BrushOr(
        Brush.verticalGradient(
            if (sortDirection == SortDirection.Asc) listOf(gradient1, gradient2, gradient3)
            else listOf(gradient3, gradient2, gradient1)
        )
    )
} else if (isHovered && isSortEnabled) {
    ColorOrBrush.ColorOr(Lsc.colors.itemHoverBg)
} else {
    ColorOrBrush.ColorOr(MaterialTheme.colors.surface)
}
