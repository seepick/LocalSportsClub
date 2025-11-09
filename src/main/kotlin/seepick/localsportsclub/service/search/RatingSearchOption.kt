package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.service.model.Rating
import seepick.localsportsclub.view.common.VisualIndicator

class RatingSearchOption<T>(
    label: String,
    private val extractor: (T) -> Rating,
    reset: () -> Unit,
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
    var searchRating: Rating by mutableStateOf(Rating.R0)
        private set

    fun updateSearchComparator(value: NumericSearchComparator) {
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
