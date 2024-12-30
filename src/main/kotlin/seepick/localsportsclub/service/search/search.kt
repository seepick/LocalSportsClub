package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.service.date.DateTimeRange

abstract class AbstractSearch<T>(
    private val resetItems: () -> Unit,
) {
    private val log = logger {}
    private val options = mutableListOf<SearchOption<T>>()
    private val predicates = mutableListOf<(T) -> Boolean>()

    protected fun newStringSearchOption(
        label: String,
        initiallyEnabled: Boolean = false,
        extractors: List<(T) -> String>
    ) =
        StringSearchOption(
            label = label,
            reset = ::reset,
            stringExtractors = extractors.toList(),
            initiallyEnabled = initiallyEnabled,
        ).also {
            options += it
        }

    protected fun newDateTimeRangeSearchOption(label: String, extractor: (T) -> DateTimeRange) =
        DateTimeRangeSearchOption(
            label = label,
            reset = ::reset,
            extractor = extractor,
        ).also {
            options += it
        }

    protected fun newBooleanSearchOption(label: String, extractor: (T) -> Boolean) =
        BooleanSearchOption(
            label = label,
            reset = ::reset,
            extractor = extractor,
        ).also {
            options += it
        }

    private fun reset() {
        predicates.clear()
        predicates.addAll(options.map { it.buildPredicateIfEnabled() })
        resetItems()
    }

    fun matches(item: T): Boolean =
        predicates.all { it(item) }
}

abstract class SearchOption<T>(
    val label: String,
    protected val reset: () -> Unit,
    initiallyEnabled: Boolean = false,
) {

    protected val alwaysTrue: (T) -> Boolean = { true }

    var enabled by mutableStateOf(initiallyEnabled)
        private set

    protected abstract fun buildPredicate(): (T) -> Boolean

    fun buildPredicateIfEnabled() =
        if (enabled) buildPredicate()
        else alwaysTrue

    fun updateEnabled(isEnabled: Boolean) {
        enabled = isEnabled
        reset()
    }
}
