package seepick.localsportsclub.view.common

import androidx.compose.foundation.lazy.LazyListState

suspend fun <T> autoScroll(listState: LazyListState, items: List<T>, selectedItem: T?) {
    val idx = selectedItem?.let { items.indexOf(it) } ?: -1
    if (idx < 0) return
    val layoutInfo = listState.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return
    val itemInfo = visibleItems.firstOrNull { it.index == idx }
    if (itemInfo == null) { // not visible at all; scroll
        listState.animateScrollToItem(idx)
        return
    }
    val viewportStart = layoutInfo.viewportStartOffset
    val viewportEnd = layoutInfo.viewportEndOffset
    val itemStart = itemInfo.offset
    val itemEnd = itemInfo.offset + itemInfo.size
    val fullyVisible = itemStart >= viewportStart && itemEnd <= viewportEnd
    if (!fullyVisible) {
        listState.animateScrollToItem(idx)
    }
}
