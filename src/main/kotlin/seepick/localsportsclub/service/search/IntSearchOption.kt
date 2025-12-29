package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.view.common.VisualIndicator

class IntSearchOption<T>(
    label: String,
    private val extractor: (T) -> Int,
    reset: () -> Unit,
    initialValue: Int? = null,
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
    var searchInt: Int? by mutableStateOf(initialValue)
        private set

    fun updateSearchComparator(value: NumericSearchComparator) {
        searchComparator = value
        reset()
    }

    fun updateSearchInt(value: Int?) {
        searchInt = value
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean =
        searchInt?.let { int ->
            { searchComparator.compareThose(extractor(it), int) }
        } ?: alwaysTruePredicate
}
