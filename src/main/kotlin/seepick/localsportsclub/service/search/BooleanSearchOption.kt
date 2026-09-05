package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.view.common.VisualIndicator

class BooleanSearchOption<T>(
    label: String,
    private val extractor: (T) -> Boolean,
    reset: () -> Unit,
    private val initialValue: Boolean = false,
    initiallyEnabled: Boolean = false,
    visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
) : SearchOption<T>(
    label = label,
    reset = reset,
    initiallyEnabled = initiallyEnabled,
    visualIndicator = visualIndicator
) {

    var searchBoolean by mutableStateOf(initialValue)
        private set

    override fun resetPermanentEnabledState() {
        searchBoolean = initialValue
    }

    fun updateSearchBoolean(boolean: Boolean) {
        searchBoolean = boolean
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean = {
        extractor(it) == searchBoolean
    }
}
