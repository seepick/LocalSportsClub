package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger

private val log = logger {}

interface Pageable {
    val showMore: Boolean
}

suspend fun <P : Pageable> fetchPageable(fetcher: suspend (Int) -> P): List<P> {
    val result = mutableListOf<P>()
    var currentPage = 1
    do {
        log.trace { "Fetching page $currentPage ..." }
        val data = fetcher(currentPage++)
        result += data
    } while (data.showMore)
    return result
}
