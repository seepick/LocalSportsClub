package seepick.localsportsclub.view.search

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.date.DateParser
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.search.DateTimeRangeSearchOption
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.DropDownTextFieldEdits
import seepick.localsportsclub.view.common.WidthOrFill
import java.time.LocalDate
import java.time.LocalTime

@Composable
@Preview
fun _Preview_DateTimeRangeSearchField() {
    DateTimeRangeSearchField(
        searchOption = DateTimeRangeSearchOption<DateTimeRange>(
            label = "label",
            reset = { println("reset") },
            extractor = { it },
            initiallyEnabled = true,
        ),
        dates = listOf(LocalDate.now())
    )
}

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
            Spacer(Modifier.width(6.dp))
            TimeRangeSelector(
                enabled = searchOption.enabled,
                onTimeSelected = searchOption::updateSearchTimeStart,
                preselectedTime = searchOption.searchTimeStart,
            )
            Text("-")
            TimeRangeSelector(
                enabled = searchOption.enabled,
                onTimeSelected = searchOption::updateSearchTimeEnd,
                preselectedTime = searchOption.searchTimeEnd,
            )
        }
    }
}

private val times: List<LocalTime> = (6..22).map {
    LocalTime.of(it, 0)
}

@Composable
@Preview
fun _Preview_TimeRangeSelector() {
    TimeRangeSelector(
        enabled = true,
        preselectedTime = null,
        onTimeSelected = {},
    )
}

@Composable
fun TimeRangeSelector(
    enabled: Boolean,
    onTimeSelected: (LocalTime?) -> Unit,
    preselectedTime: LocalTime?,
) {
    val timeAsString = remember { mutableStateOf(preselectedTime?.prettyPrint() ?: "") }
    DropDownTextField(
        items = times,
        selectedItem = preselectedTime,
        itemFormatter = { it?.prettyPrint() ?: "" },
        onItemSelected = {
            timeAsString.value = it?.prettyPrint() ?: ""
            onTimeSelected(it)
        },
        useSlimDisplay = true,
        enabled = enabled,
        textSize = WidthOrFill.Width(140.dp),
        textFieldEdits = DropDownTextFieldEdits(
            text = timeAsString,
            onTextChanged = { enteredString: String ->
                timeAsString.value = enteredString
                if (enteredString.isEmpty()) onTimeSelected(null)
                else DateParser.parseTimeOrNull(enteredString)?.also { enteredTime ->
                    onTimeSelected(enteredTime)
                }
            },
            errorChecker = { if (timeAsString.value.isEmpty()) false else DateParser.parseTimeOrNull(timeAsString.value) == null },
            textAlign = TextAlign.Center,
            onReset = {
                timeAsString.value = ""
                onTimeSelected(null)
            }
        ),
    )
}
