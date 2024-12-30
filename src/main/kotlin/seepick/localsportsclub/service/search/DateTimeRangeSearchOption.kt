package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.service.date.DateTimeRange
import java.time.LocalDate
import java.time.LocalTime

class DateTimeRangeSearchOption<T>(
    label: String,
    reset: () -> Unit,
    private val extractor: (T) -> DateTimeRange,
) : SearchOption<T>(label, reset) {

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

    fun updateSearchTimeStart(time: LocalTime?) {
        searchTimeStart = time
        reset()
    }

    fun updateSearchTimeEnd(time: LocalTime?) {
        searchTimeEnd = time
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean =
        if (searchDate == null) alwaysTrue
        else { item ->
            val itemDateItemRange = extractor(item)
            itemDateItemRange.isStartMatching(searchDate!!, timeFrom = searchTimeStart, timeTo = searchTimeEnd)
        }
}
