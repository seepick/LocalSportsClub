package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.service.model.Rating

class RatingSearchOption<T>(
    label: String,
    private val extractor: (T) -> Rating,
    reset: () -> Unit,
    initiallyEnabled: Boolean = false,
) : SearchOption<T>(label = label, reset = reset, initiallyEnabled = initiallyEnabled) {

    var searchComparator: IntSearchComparator by mutableStateOf(IntSearchComparator.Equals)
        private set
    var searchRating: Rating by mutableStateOf(Rating.R0)
        private set

    fun updateSearchComparator(value: IntSearchComparator) {
        searchComparator = value
        reset()
    }

    fun updateSearchRating(value: Rating) {
        searchRating = value
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean = {
        searchComparator.compareTo(extractor(it).value, searchRating.value)
    }
}
