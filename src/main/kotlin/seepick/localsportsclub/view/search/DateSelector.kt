package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.api.UscConfig
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import java.time.LocalDate

@Composable
fun DateSelector(
    enabled: Boolean,
    searchDate: LocalDate?,
    initializeDate: (LocalDate) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    clock: Clock = koinInject(),
    uscConfig: UscConfig = koinInject(),
) {
    val today = clock.today()
    val dates = (0..<uscConfig.syncDaysAhead).map { today.plusDays(it.toLong()) }
    if (searchDate == null) {
        initializeDate(dates.first())
    }
    var currentDateIndex by mutableStateOf(dates.indexOf(searchDate))
    Button(onClick = {
        currentDateIndex--
        onDateSelected(dates[currentDateIndex])
    }, enabled = enabled && currentDateIndex > 0) {
        Text("<")
    }
    BasicTextField(
        value = searchDate?.prettyPrint(today.year) ?: "",
        onValueChange = {},
        singleLine = true,
        readOnly = true,
        enabled = enabled,
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        modifier = Modifier.width(100.dp),
    )
    Button(onClick = {
        currentDateIndex++
        onDateSelected(dates[currentDateIndex])
    }, enabled = enabled && currentDateIndex < (dates.size - 1)) {
        Text(">")
    }
}
