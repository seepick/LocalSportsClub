package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollbarAdapter
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun <T> Table(
    items: List<T>,
    columns: List<TableColumn<T>>,
    selectedItem: T? = null,
    onItemClicked: ((T) -> Unit)?,
    onHeaderClicked: (TableColumn<T>) -> Unit = {},
    sortColumn: TableColumn<T>?,
    headerEnabled: Boolean = true,
    itemsLabel: String? = null,
    allItemsCount: Int? = null,
    boxModifier: Modifier = Modifier,
    columnModifier: Modifier = Modifier,
) {
    val colorEvenRow = if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray
    Box(modifier = boxModifier) {
        val tableScrollState = rememberLazyListState()
        LazyColumn(
            state = tableScrollState,
            modifier = Modifier.padding(
                end = 12.dp, // for the scrollbar to the right
            ).let {
                if (itemsLabel == null) it else it.padding(bottom = 14.dp)
            }.then(columnModifier),
        ) {
            if (headerEnabled) {
                stickyHeader {
                    Row(Modifier) {
                        columns.forEach { col ->
                            TableHeader(
                                text = col.headerLabel ?: error("Missing header label for: $col"),
                                size = col.size,
                                isSortEnabled = col.sortingEnabled,
                                isSortActive = col == sortColumn,
                                onClick = { onHeaderClicked(col) },
                            )
                        }
                    }
                }
            }
            itemsIndexed(items) { index, item ->
                var isHovered by remember { mutableStateOf(false) }
                val rowColor = if (index % 2 == 0) {
                    colorEvenRow
                } else {
                    MaterialTheme.colors.background
                }
                val bgColor = if (onItemClicked != null) {
                    if (isHovered) Color.Green else if (selectedItem == item) Color.Red else rowColor
                } else {
                    if (selectedItem == item) Color.Red else rowColor
                }
                Row(Modifier/*.fillMaxWidth()*/.background(color = bgColor)
                    .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                    .onPointerEvent(PointerEventType.Exit) { isHovered = false }
                    // https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Mouse_Events#mouse-event-listeners
                    .let {
                        if (onItemClicked == null) it
                        else it.onClick { onItemClicked(item) }
                    }) {
                    columns.forEach { col ->
                        when (col.renderer) {
                            is CellRenderer.CustomRenderer -> {
                                col.renderer.invoke(this, item, col)
                            }

                            is CellRenderer.TextRenderer -> {
                                TableCell(
                                    text = col.renderer.extractor(item).toString(),
                                    size = col.size,
                                )
                            }
                        }
                    }
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(), adapter = rememberScrollbarAdapter(
                scrollState = tableScrollState
            )
        )
        if (itemsLabel != null) {
            Text(
                // TODO allItemsCount also includes hidden items; but it shouldn't. adjust above.
                text = " Showing ${items.size} " + (if (allItemsCount != null) "of $allItemsCount " else "") + itemsLabel,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}
