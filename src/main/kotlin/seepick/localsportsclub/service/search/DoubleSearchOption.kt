package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.view.common.VisualIndicator

class DoubleSearchOption<T>(
    label: String,
    private val extractor: (T) -> Double,
    reset: () -> Unit,
    initialValue: Double? = null,
    initiallyEnabled: Boolean = false,
    visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
) : SearchOption<T>(
    label = label,
    reset = reset,
    permanentEnabled = initiallyEnabled,
    visualIndicator = visualIndicator,
) {

    var searchComparator: NumericSearchComparator by mutableStateOf(NumericSearchComparator.Equals)
        private set
    var searchDouble: Double? by mutableStateOf(initialValue)
        private set

    fun updateSearchComparator(value: NumericSearchComparator) {
        searchComparator = value
        reset()
    }

    fun updateSearchDouble(value: Double?) {
        searchDouble = value
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean =
        searchDouble?.let { double ->
            { searchComparator.compareTo(extractor(it), double) }
        } ?: alwaysTruePredicate
}
