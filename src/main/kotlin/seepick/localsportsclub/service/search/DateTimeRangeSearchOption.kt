package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.seepick.uscclient.shared.DateTimeRange
import seepick.localsportsclub.service.date.prettyPrint
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
    // decouple rendered string from actual search value
    val searchTimeStartString = mutableStateOf("")
    val searchTimeEndString = mutableStateOf("")

    fun updateSearchDate(date: LocalDate) {
        searchDate = date
        reset()
    }

    fun updateSearchTimeStart(time: LocalTime?): LocalTime? {
        if (searchTimeStart == time) return searchTimeStart
        if (time != null && searchTimeEnd != null && time.isAfter(searchTimeEnd)) {
            searchTimeEnd = time
            searchTimeEndString.value = time.prettyPrint()
        }
        searchTimeStart = time
        reset()
        return searchTimeStart
    }

    fun updateSearchTimeEnd(time: LocalTime?): LocalTime? {
        if (searchTimeEnd == time) return searchTimeEnd
        if (time != null && searchTimeStart != null && time.isBefore(searchTimeStart)) {
            searchTimeStart = time
            searchTimeStartString.value = time.prettyPrint()
        }
        searchTimeEnd = time
        reset()
        return searchTimeEnd
    }

    override fun buildPredicate(): (T) -> Boolean =
        if (searchDate == null) alwaysTruePredicate
        else { item ->
            val itemDateItemRange = extractor(item)
            itemDateItemRange.isFromMatching(searchDate!!, matchFrom = searchTimeStart, matchTo = searchTimeEnd)
        }
}
