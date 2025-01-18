package seepick.localsportsclub.service

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.view.common.table.TableColumn

enum class SortDirection {
    Asc, Desc;

    fun toggle() = if (this == Asc) Desc else Asc
}

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
    initialSortColumn: TableColumn<T>? = null,
) {

    private val log = logger {}
    val selectedColumnValueExtractor get() = sortColumn.sortValueExtractor!!
    var sortColumn: TableColumn<T> by mutableStateOf(initialSortColumn ?: columns.first { it.sortingEnabled })
        private set
    var sortDirection: SortDirection by mutableStateOf(SortDirection.Asc)
        private set

    fun onSortColumn(column: TableColumn<T>) {
        log.debug { "update sorting for: ${column.headerLabel}" }
        require(sortColumn.sortingEnabled)
        if (sortColumn == column) {
            sortDirection = sortDirection.toggle()
        } else {
            sortColumn = column
            sortDirection = SortDirection.Asc
        }
        resetSort()
    }

    fun sortIt(items: List<T>): List<T> {
        val selector = { item: T ->
            @Suppress("UNCHECKED_CAST")
            sortColumn.sortValueExtractor!!.invoke(item) as? Comparable<Any>
        }
        return when (sortDirection) {
            SortDirection.Asc -> items.sortedBy(selector)
            SortDirection.Desc -> items.sortedByDescending(selector)
        }
    }
}
