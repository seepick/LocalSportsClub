package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

class DateSearchOption<T>(
    label: String,
    reset: () -> Unit,
    private val dateExtractor: (T) -> LocalDate,
    initialDate: LocalDate,
    initiallyEnabled: Boolean = false,
) : SearchOption<T>(label, reset, initiallyEnabled) {

    var searchDate: LocalDate by mutableStateOf(initialDate)
        private set

    fun updateSearchDate(date: LocalDate) {
        searchDate = date
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean =
        { item -> searchDate == dateExtractor(item) }
}
