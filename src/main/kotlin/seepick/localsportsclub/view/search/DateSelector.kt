package seepick.localsportsclub.view.search

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.SmallButton
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

private val hspacing = 4.dp
private val elementsHeight = 26.dp
private val buttonWidth = 26.dp
private val buttonSize = DpSize(width = buttonWidth, height = elementsHeight)

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
    SmallButton(
        label = "<",
        size = buttonSize,
        enabled = enabled && currentDateIndex > 0,
    ) {
        currentDateIndex--
        onDateSelected(dates[currentDateIndex])
    }
    Spacer(modifier = Modifier.width(hspacing))
    DropDownTextField(
        items = dates,
        enabled = enabled,
        itemFormatter = { it?.prettyPrint(today.year) ?: "-" },
        onItemSelected = { onDateSelected(it!!) },
        selectedItem = selectedDate,
        textSize = WidthOrFill.Width(150.dp),
        useSlimDisplay = true,
    )
    Spacer(modifier = Modifier.width(hspacing))
    SmallButton(
        label = ">",
        size = buttonSize,
        enabled = enabled && currentDateIndex < (dates.size - 1),
    ) {
        currentDateIndex++
        onDateSelected(dates[currentDateIndex])
    }
}
