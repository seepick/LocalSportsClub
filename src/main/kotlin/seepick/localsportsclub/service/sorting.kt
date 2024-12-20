package seepick.localsportsclub.service

fun <T, R : Comparable<R>> searchIndexFor(items: List<T>, pivot: T, extractor: (T) -> R): Int {
    var index = -1
    val pivotValue = extractor(pivot)
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
