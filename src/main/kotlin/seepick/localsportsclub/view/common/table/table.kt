package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.delay
import seepick.localsportsclub.service.SortDirection
import seepick.localsportsclub.view.common.LscVScroll
import seepick.localsportsclub.view.common.autoScroll
import seepick.localsportsclub.view.common.scrollbarWidthPadding

interface TableItemBgColor {
    val tableBgColor: Color?
}

interface TableItemAlpha {
    val isTransparent: Boolean
}

enum class VDirection {
    Up, Down;
}

fun <T> List<T>.navigate(currentlySelected: T, direction: VDirection): T? {
    val index = indexOf(currentlySelected)
    return when (direction) {
        VDirection.Up -> {
            if (index > 0) this[index - 1] else null
        }

        VDirection.Down -> {
            if (index < (this.size - 1)) this[index + 1] else null
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun <T> MainTable(
    items: List<T>,
    columns: List<TableColumn<T>>,
    selectedItem: T? = null,
    onItemClicked: ((T) -> Unit)?,
    onItemNavigation: ((VDirection, T) -> Unit)? = null,
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
    var activeNavigation by remember { mutableStateOf<VDirection?>(null) }
    LaunchedEffect(activeNavigation, selectedItem) {
        if (activeNavigation != null && selectedItem != null && onItemNavigation != null) {
            delay(200)
            while (activeNavigation != null) {
                onItemNavigation(activeNavigation!!, selectedItem)
                delay(200)
            }
        }
    }
    Box(
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged { state ->
                isFocused = state.isFocused
            }
            .onKeyEvent {
                if (selectedItem != null && onItemNavigation != null && isFocused) {
                    if (it.type == KeyEventType.KeyDown && activeNavigation == null) {
                        val nav = when {
                            it.key == Key.DirectionUp -> VDirection.Up
                            it.key == Key.DirectionDown -> VDirection.Down
                            else -> null
                        }
                        if (nav != null) {
                            activeNavigation = nav
                            onItemNavigation(nav, selectedItem)
                        }
                    } else if (it.type == KeyEventType.KeyUp && (it.key == Key.DirectionUp || it.key == Key.DirectionDown)) {
                        activeNavigation = null
                    }
                }
                false
            }
            .focusProperties { canFocus = true }
            .then(boxModifier)
    ) {
        val tableScrollState = rememberLazyListState()
        LaunchedEffect(items, selectedItem) {
            autoScroll(tableScrollState, items, selectedItem)
        }
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

                Row(
                    Modifier
                        .background(color = bgColor)
                        .let { if (item is TableItemAlpha && item.isTransparent) it.alpha(0.4f) else it }
                        .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                        .onPointerEvent(PointerEventType.Exit) { isHovered = false }
                        // https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Mouse_Events#mouse-event-listeners
                        .let {
                            if (onItemClicked == null) it
                            else it.onClick {
                                focusRequester.requestFocus()
                                onItemClicked(item)
                            }
                        }) {
                    columns.forEach { col ->
                        renderComposable(item, col)
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

