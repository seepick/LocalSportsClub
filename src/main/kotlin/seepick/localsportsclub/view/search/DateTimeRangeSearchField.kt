package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.date.DateParser
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.search.DateTimeRangeSearchOption
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.DropDownTextFieldEdits
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun <T> DateTimeRangeSearchField(
    searchOption: DateTimeRangeSearchOption<T>,
    dates: List<LocalDate>,
) {
    if (searchOption.searchDate == null) searchOption.updateSearchDate(dates.first())
    Row(verticalAlignment = Alignment.CenterVertically) {
        searchOption.ClickableSearchText()
        if (searchOption.enabled) {
            DateSelector(
                enabled = searchOption.enabled,
                dates = dates,
                selectedDate = searchOption.searchDate,
                onDateSelected = searchOption::updateSearchDate,
            )
            TimeRangeSelector(
                enabled = searchOption.enabled,
                onTimeSelected = searchOption::updateSearchTimeStart,
                selectedTime = searchOption.searchTimeStart,
            )
            Text("-")
            TimeRangeSelector(
                enabled = searchOption.enabled,
                onTimeSelected = searchOption::updateSearchTimeEnd,
                selectedTime = searchOption.searchTimeEnd,
            )
        }
    }
}

private val times: List<LocalTime> = (6..22).map {
    LocalTime.of(it, 0)
}

@Composable
fun TimeRangeSelector(
    enabled: Boolean,
    onTimeSelected: (LocalTime?) -> Unit,
    selectedTime: LocalTime?,
) {
    var time: LocalTime? by remember { mutableStateOf(null) }
    val timeAsString = remember { mutableStateOf("") }

    DropDownTextField(
        items = times,
        selectedItem = selectedTime,
        itemFormatter = { it?.prettyPrint() ?: "" },
        onItemSelected = {
            time = it
            timeAsString.value = it?.prettyPrint() ?: ""
            onTimeSelected(it)
        },
        enabled = enabled,
        textWidth = 160.dp,
        textFieldEdits = DropDownTextFieldEdits(
            text = timeAsString,
            onTextChanged = { enteredString: String ->
                timeAsString.value = enteredString
                if (enteredString.isEmpty()) onTimeSelected(null)
                else DateParser.parseTimeOrNull(enteredString)?.also { enteredTime ->
                    time = enteredTime
                    onTimeSelected(enteredTime)
                }
            },
            errorChecker = { if (timeAsString.value.isEmpty()) false else DateParser.parseTimeOrNull(timeAsString.value) == null },
            textAlign = TextAlign.Center,
            onReset = {
                time = null
                timeAsString.value = ""
                onTimeSelected(null)
            }
        ),
    )
}
