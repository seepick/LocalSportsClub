package seepick.localsportsclub.view.search

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.WidthOrFill
import java.time.LocalDate

@Composable
@Preview
fun _Preview_DateSelector() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DateSelector(
            enabled = true,
            selectedDate = SystemClock.today(),
            onDateSelected = {},
            dates = listOf(),
            clock = SystemClock,
        )
    }
}

@Composable
fun DateSelector(
    enabled: Boolean,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    dates: List<LocalDate>,
    clock: Clock = koinInject(),
) {
    val today = clock.today()
    var currentDateIndex by mutableStateOf(dates.indexOf(selectedDate))
    Button(onClick = {
        currentDateIndex--
        onDateSelected(dates[currentDateIndex])
    }, enabled = enabled && currentDateIndex > 0) {
        Text("<")
    }

    DropDownTextField(
        items = dates,
        enabled = enabled,
        itemFormatter = { it?.prettyPrint(today.year) ?: "-" },
        onItemSelected = { onDateSelected(it!!) },
        selectedItem = selectedDate,
        textSize = WidthOrFill.Width(150.dp),
    )

    Button(onClick = {
        currentDateIndex++
        onDateSelected(dates[currentDateIndex])
    }, enabled = enabled && currentDateIndex < (dates.size - 1)) {
        Text(">")
    }
}
