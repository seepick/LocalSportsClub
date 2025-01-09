package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.search.DateSearchOption

@Composable
fun DateSearchField(searchOption: DateSearchOption<Freetraining>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(searchOption.label)
        Checkbox(checked = searchOption.enabled, onCheckedChange = { searchOption.updateEnabled(it) })
        DateSelector(
            enabled = searchOption.enabled,
            searchDate = searchOption.searchDate,
            initializeDate = searchOption::initializeDate,
            onDateSelected = searchOption::updateSearchDate
        )
    }
}
