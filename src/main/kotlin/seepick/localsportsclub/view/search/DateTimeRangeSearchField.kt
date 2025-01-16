package seepick.localsportsclub.view.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import seepick.localsportsclub.service.date.DateParser
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.search.DateTimeRangeSearchOption
import seepick.localsportsclub.view.common.LscDropdownMenu
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun <T> DateTimeRangeSearchField(
    searchOption: DateTimeRangeSearchOption<T>,
    dates: List<LocalDate>,
) {
    if (searchOption.searchDate == null) searchOption.updateSearchDate(dates.first())
    Row(verticalAlignment = Alignment.CenterVertically) {
        searchOption.buildClickableText()
        if (searchOption.enabled) {
            DateSelector(
                enabled = searchOption.enabled,
                dates = dates,
                searchDate = searchOption.searchDate,
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
    var timeAsString by remember { mutableStateOf("") }

    var isMenuExpanded = remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val icon = if (isMenuExpanded.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    Column {
        TextField(
            value = timeAsString,
            enabled = enabled,
            isError = if (timeAsString.isEmpty()) false else DateParser.parseTimeOrNull(timeAsString) == null,
            onValueChange = { enteredString ->
                timeAsString = enteredString
                if (enteredString.isEmpty()) onTimeSelected(null)
                else DateParser.parseTimeOrNull(enteredString)?.also { enteredTime ->
                    time = enteredTime
                    onTimeSelected(enteredTime)
                }
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            modifier = Modifier.width(160.dp).onGloballyPositioned { coordinates ->
                textFieldSize = coordinates.size.toSize()
            }
                .onPreviewKeyEvent { e ->
                    if (e.key == Key.Escape && e.type == KeyEventType.KeyUp) {
                        time = null
                        timeAsString = ""
                        onTimeSelected(null)
                    }
                    false
                },

            leadingIcon = {
                Icon(Icons.Default.Close, null, Modifier.let {
                    if (enabled) {
                        it.clickable {
                            time = null
                            timeAsString = ""
                            onTimeSelected(null)
                        }
                    } else it
                })
            },
            trailingIcon = {
                Icon(icon, null, Modifier.let {
                    if (enabled) {
                        it.clickable {
                            isMenuExpanded.value = !isMenuExpanded.value
                        }
                    } else it
                })
            },
        )
        LscDropdownMenu(
            items = times,
            isMenuExpanded = isMenuExpanded,
            textFieldSize = textFieldSize,
            onItemClicked = {
                time = it
                timeAsString = it?.prettyPrint() ?: ""
                onTimeSelected(it)
            },
            selectedItem = selectedTime,
            itemFormatter = { it?.prettyPrint() ?: "" }
        )
    }
}
