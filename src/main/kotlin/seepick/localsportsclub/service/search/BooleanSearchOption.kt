package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BooleanSearchOption<T>(
    label: String,
    private val extractor: (T) -> Boolean,
    reset: () -> Unit,
) : SearchOption<T>(label, reset) {

    var searchBoolean by mutableStateOf(false)
        private set

    fun updateSearchBoolean(boolean: Boolean) {
        searchBoolean = boolean
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean = {
        extractor(it) == searchBoolean
    }
}
