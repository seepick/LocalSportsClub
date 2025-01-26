package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.view.common.HasLabel
import seepick.localsportsclub.view.common.VisualIndicator

enum class IntSearchComparator(
    val symbol: String,
    val compareTo: (Int, Int) -> Boolean
) : HasLabel {
    Equals("=", { x, y -> x == y }),
    Not("!=", { x, y -> x != y }),
    Lower("<", { x, y -> x < y }),
    Bigger(">", { x, y -> x > y });

    override val label = symbol
}

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
    initiallyEnabled = initiallyEnabled,
    visualIndicator = visualIndicator,
) {

    var searchComparator: IntSearchComparator by mutableStateOf(IntSearchComparator.Equals)
        private set
    var searchInt: Int? by mutableStateOf(initialValue)
        private set

    fun updateSearchComparator(value: IntSearchComparator) {
        searchComparator = value
        reset()
    }

    fun updateSearchInt(value: Int?) {
        searchInt = value
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean =
        searchInt?.let { int ->
            { searchComparator.compareTo(extractor(it), int) }
        } ?: alwaysTruePredicate
}
