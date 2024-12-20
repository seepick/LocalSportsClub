package seepick.localsportsclub.view.table

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollbarAdapter
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun <T> Table(
    items: List<T>,
    columns: List<TableColumn<T>>,
    onItemClicked: (T) -> Unit,
    onHeaderClicked: (TableColumn<T>) -> Unit,
    sortColumn: TableColumn<T>,
) {
    Box {
        val tableScrollState = rememberLazyListState()
        LazyColumn(
            state = tableScrollState,
            modifier = Modifier
                .padding(end = 12.dp), // for the scrollbar to the right
        ) {
            stickyHeader {
                Row(Modifier.background(Color.Gray)) {
                    columns.forEach { col ->
                        TableHeader(
                            text = col.headerLabel,
                            size = col.size,
                            isSortActive = col == sortColumn,
                            onClick = { onHeaderClicked(col) },
                        )
                    }
                }
            }
            items(items) { item ->
                var isHovered by remember { mutableStateOf(false) }
                Row(Modifier.fillMaxWidth().background(color = if (isHovered) Color.Green else Color.White)
                    .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                    .onPointerEvent(PointerEventType.Exit) { isHovered = false }
                    // https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Mouse_Events#mouse-event-listeners
                    .onClick { onItemClicked(item) }
                ) {
                    columns.forEach { col ->
                        when (col.renderer) {
                            is CellRenderer.CustomRenderer -> {
                                col.renderer.invoke(this, item, col.size)
                            }

                            is CellRenderer.TextRenderer -> {
                                TableData(
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
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = tableScrollState
            )
        )
    }
}
