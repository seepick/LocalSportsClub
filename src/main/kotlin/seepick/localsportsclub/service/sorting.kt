package seepick.localsportsclub.service

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.view.common.table.TableColumn

fun <T, R> findIndexFor(items: List<T>, pivot: T, extractor: (T) -> R): Int {
    var index = -1
    @Suppress("UNCHECKED_CAST") val pivotValue = extractor(pivot) as Comparable<R>
    for (i in items.indices) {
        val currentValue = extractor(items[i])
        val compareResult = pivotValue.compareTo(currentValue)
        if (compareResult == 0) {
            index = i + 1
            break
        }
        if (compareResult < 0) {
            index = i
            break
        }
    }
    if (index == -1) {
        index = items.size
    }
    return index
}

class SortingDelegate<T>(
    columns: List<TableColumn<T>>,
    private val resetSort: () -> Unit,
) {

    private val log = logger {}
    val selectedColumnValueExtractor get() = sortColumn.sortValueExtractor!!
    var sortColumn: TableColumn<T> by mutableStateOf(columns.first { it.sortingEnabled })
        private set

    fun onHeaderClicked(column: TableColumn<T>) {
        if (sortColumn == column) return
        require(sortColumn.sortingEnabled)
        log.debug { "update sorting for: ${column.headerLabel}" }
        sortColumn = column
        resetSort()
    }

    fun sortIt(items: List<T>): List<T> =
        items.sortedBy { item ->
            @Suppress("UNCHECKED_CAST")
            sortColumn.sortValueExtractor!!.invoke(item) as? Comparable<Any>

        }
}
