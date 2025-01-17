package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.search.DateSearchOption
import java.time.LocalDate

@Composable
fun DateSearchField(
    searchOption: DateSearchOption<Freetraining>,
    dates: List<LocalDate>,
) {
    if (searchOption.searchDate == null) searchOption.updateSearchDate(dates.first())
    Row(verticalAlignment = Alignment.CenterVertically) {
        searchOption.ClickableSearchText()
        if (searchOption.enabled) {
            DateSelector(
                enabled = searchOption.enabled,
                selectedDate = searchOption.searchDate,
                dates = dates,
                onDateSelected = searchOption::updateSearchDate
            )
        }
    }
}
