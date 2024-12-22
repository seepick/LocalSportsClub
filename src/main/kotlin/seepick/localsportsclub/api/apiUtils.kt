package seepick.localsportsclub.api

interface Pageable {
    val showMore: Boolean
}

suspend fun <P : Pageable> fetchPageable(fetcher: suspend (Int) -> P): List<P> {
    val result = mutableListOf<P>()
    var currentPage = 1
    do {
        val data = fetcher(currentPage++)
        result += data
    } while (data.showMore)
    return result
}
