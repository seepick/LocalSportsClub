package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.SortDirection
import seepick.localsportsclub.view.common.LscVScroll
import seepick.localsportsclub.view.common.rowBgColor

interface TableItemBgColor {
    val tableBgColor: Color?
}

private val scrollbarWidthPadding = 12.dp

enum class TableNavigation {
    Up, Down;
}

fun <T> List<T>.navigate(currentlySelected: T, navigation: TableNavigation): T? {
    val index = indexOf(currentlySelected)
    return when (navigation) {
        TableNavigation.Up -> {
            if (index > 0) this[index - 1] else null
        }

        TableNavigation.Down -> {
            if (index < (this.size - 1)) this[index + 1] else null
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun <T> Table(
    items: List<T>,
    columns: List<TableColumn<T>>,
    selectedItem: T? = null,
    onItemClicked: ((T) -> Unit)?,
    onItemNavigation: ((TableNavigation, T) -> Unit)? = null,
    onHeaderClicked: (TableColumn<T>) -> Unit = {},
    sortColumn: TableColumn<T>?,
    sortDirection: SortDirection,
    headerEnabled: Boolean = true,
    customTableItemBgColorEnabled: Boolean = false,
    itemsLabel: String? = null,
    allItemsCount: Int? = null,
    boxModifier: Modifier = Modifier,
    columnModifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .focusRequester(focusRequester)
        .onFocusChanged { state ->
            println("onFocusChanged: $state")
            isFocused = state.isFocused
        }
        .onKeyEvent {
            if (selectedItem != null && onItemNavigation != null && isFocused && it.type == KeyEventType.KeyUp) {
                if (it.key == Key.DirectionUp) {
                    onItemNavigation(TableNavigation.Up, selectedItem)
                } else if (it.key == Key.DirectionDown) {
                    onItemNavigation(TableNavigation.Down, selectedItem)
                }
            }
            false
        }
        .focusProperties { canFocus = true }
        .border(1.dp, if (isFocused) Lsc.colors.primary else Color.Gray)
        .then(boxModifier)) {

        val tableScrollState = rememberLazyListState()
        LazyColumn(
            state = tableScrollState,
            modifier = Modifier.padding(
                end = scrollbarWidthPadding,
            ).let {
                if (itemsLabel == null) it else it.padding(bottom = 14.dp)
            }.then(columnModifier),
        ) {
            if (headerEnabled) {
                renderTableHeader(columns, sortColumn, sortDirection, onHeaderClicked = {
                    focusRequester.requestFocus()
                    onHeaderClicked(it)
                })
            }
            itemsIndexed(items) { index, item ->
                var isHovered by remember { mutableStateOf(false) }

                val bgColor = rowBgColor(
                    index = index,
                    isHovered = isHovered,
                    isSelected = selectedItem == item,
                    isClickable = onItemClicked != null,
                    primaryColor = if (customTableItemBgColorEnabled && item is TableItemBgColor) item.tableBgColor else null
                )

                Row(Modifier.background(color = bgColor)
                    .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                    .onPointerEvent(PointerEventType.Exit) { isHovered = false }
                    // https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Mouse_Events#mouse-event-listeners
                    .let {
                        if (onItemClicked == null) it
                        else it.onClick {
                            println("request focus; row")
                            focusRequester.requestFocus()
                            onItemClicked(item)
                        }
                    }) {
                    columns.forEach { col ->
                        when (col.renderer) {
                            is CellRenderer.CustomRenderer -> {
                                col.renderer.invoke(this, item, col)
                            }

                            is CellRenderer.TextRenderer -> {
                                TableTextCell(
                                    text = col.renderer.extractor(item).toString(),
                                    size = col.size,
                                    textAlign = col.renderer.textAlign,
                                    modifier = Modifier.let { m1 ->
                                        val m2 = if (col.renderer.paddingLeft) {
                                            m1.padding(start = 8.dp)
                                        } else m1
                                        val m3 = if (col.renderer.paddingRight) {
                                            m2.padding(end = 8.dp)
                                        } else m2
                                        m3
                                    })
                            }
                        }
                    }
                }
            }
        }
        LscVScroll(rememberScrollbarAdapter(tableScrollState))
        if (itemsLabel != null) {
            Text(
                text = " Showing ${items.size} " + (if (allItemsCount != null) "of $allItemsCount " else "") + itemsLabel,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

