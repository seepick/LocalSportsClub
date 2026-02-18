package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.seepick.uscclient.utils.DateTimeRange
import seepick.localsportsclub.view.common.VisualIndicator
import java.time.LocalDate
import java.time.LocalTime

class DateTimeRangeSearchOption<T>(
    label: String,
    reset: () -> Unit,
    private val extractor: (T) -> DateTimeRange,
    initiallyEnabled: Boolean = false,
    visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
) : SearchOption<T>(label, reset, initiallyEnabled, visualIndicator) {

    var searchDate: LocalDate? by mutableStateOf(null)
        private set
    var searchTimeStart: LocalTime? by mutableStateOf(null)
        private set
    var searchTimeEnd: LocalTime? by mutableStateOf(null)
        private set


    fun updateSearchDate(date: LocalDate) {
        searchDate = date
        reset()
    }

    fun initializeDate(date: LocalDate) {
        searchDate = date
        // no reset()
    }

    fun updateSearchTimeStart(time: LocalTime?) {
        searchTimeStart = time
        reset()
    }

    fun updateSearchTimeEnd(time: LocalTime?) {
        searchTimeEnd = time
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean =
        if (searchDate == null) alwaysTruePredicate
        else { item ->
            val itemDateItemRange = extractor(item)
            itemDateItemRange.isStartMatching(searchDate!!, matchFrom = searchTimeStart, matchTo = searchTimeEnd)
        }
}
