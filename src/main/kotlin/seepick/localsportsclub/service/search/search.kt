package seepick.localsportsclub.service.search

import io.github.oshai.kotlinlogging.KotlinLogging.logger

abstract class AbstractSearch<T>(
    private val resetItems: () -> Unit,
) {
    private val log = logger {}
    private val options = mutableListOf<SearchOption<T>>()
    private val predicates = mutableListOf<(T) -> Boolean>()

    protected fun buildSearchOption(label: String, vararg stringExtractors: (T) -> String) =
        StringSearchOption(
            label = label,
            stringExtractors = stringExtractors.toList(),
            resetPredicates = ::resetPredicates,
            resetItems = resetItems,
        ).also {
            options += it
        }

    private fun resetPredicates() {
        predicates.clear()
        predicates.addAll(options.map { it.buildPredicate() })
    }

    fun matches(item: T): Boolean =
        predicates.all { it(item) }
}

abstract class SearchOption<T>(val label: String) {
    protected val alwaysTrue: (T) -> Boolean = { true }
    abstract fun buildPredicate(): (T) -> Boolean
}
