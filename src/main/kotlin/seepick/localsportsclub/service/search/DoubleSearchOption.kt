package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.view.common.VisualIndicator

class DoubleSearchOption<T, C : NumericComparator>(
    initialComparator: C,
    label: String,
    private val extractor: (T) -> Double,
    reset: () -> Unit,
    private val initialValue: Double? = null,
    initiallyEnabled: Boolean = false,
    visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
) : SearchOption<T>(
    label = label,
    reset = reset,
    permanentEnabled = initiallyEnabled,
    visualIndicator = visualIndicator,
) {
    private val log = logger {}
    var searchComparator: C by mutableStateOf(initialComparator)
        private set
    var searchOperand: Double? by mutableStateOf(initialValue)
        private set
    // decouple rendered string from actual search value
    var searchString: String by mutableStateOf(initialValue?.toString() ?: "")
        private set

    fun updateSearchComparator(value: C) {
        log.debug { "updateSearchComparator($value)" }
        searchComparator = value
        reset()
    }

    fun updateSearchString(value: String) {
        log.debug { "updateSearchString($value)" }
        searchString = value
        val normalized = value.replace(',', '.')
        val isAllowed = normalized.isEmpty() || normalized.matches(Regex("""\d*([.]\d*)?"""))
        if (isAllowed) {
            normalized.toDoubleOrNull()?.let {
                updateSearchOperand(it)
            }
        }
    }

    private fun updateSearchOperand(value: Double?) {
        log.debug { "updateSearchOperand($value)" }
        searchOperand = value
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean =
        searchOperand?.let { operand ->
            { searchComparator.compareThose(extractor(it), operand) }
        } ?: alwaysTruePredicate

    fun reinitializeState() { // on focus lost
        updateSearchString((searchOperand ?: initialValue)?.toString() ?: "")
    }
}
