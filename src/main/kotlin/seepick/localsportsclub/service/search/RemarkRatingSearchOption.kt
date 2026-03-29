package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.service.model.RemarkRating
import seepick.localsportsclub.view.common.VisualIndicator

class RemarkRatingSearchOption<T>(
    label: String,
    private val extractor: (T) -> RemarkRating?,
    reset: () -> Unit,
    initiallyEnabled: Boolean = false,
    visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
) : SearchOption<T>(
    label = label,
    reset = reset,
    initiallyEnabled = initiallyEnabled,
    visualIndicator = visualIndicator,
) {
    var searchComparator: FullNumericComparator by mutableStateOf(FullNumericComparator.Bigger)
        private set
    var searchRating: RemarkRating by mutableStateOf(RemarkRating.Meh)
        private set

    fun updateSearchComparator(value: FullNumericComparator) {
        searchComparator = value
        reset()
    }

    fun updateSearchRating(value: RemarkRating) {
        searchRating = value
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean = {
        val extracted = extractor(it)

        if (extracted != null) {
            searchComparator.compareThose(extracted.numericValue, searchRating.numericValue)
        } else {
            searchComparator == FullNumericComparator.Not // if operator is "!=" & rating is null => true
        }
    }
}
