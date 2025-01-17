package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

class DateSearchOption<T>(
    label: String,
    reset: () -> Unit,
    private val dateExtractor: (T) -> LocalDate,
    initiallyEnabled: Boolean = false,
) : SearchOption<T>(label, reset, initiallyEnabled) {

    // TODO make searchDate: LocalDate non-nullable; preselect in call hierarchy above
    var searchDate: LocalDate? by mutableStateOf(null)
        private set

    fun updateSearchDate(date: LocalDate) {
        searchDate = date
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean =
        searchDate?.let { date ->
            { item -> date == dateExtractor(item) }
        } ?: alwaysTruePredicate
}
