package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import java.time.LocalDate

@Composable
fun DateSelector(
    enabled: Boolean,
    searchDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    dates: List<LocalDate>,
    clock: Clock = koinInject(),
) {
    val today = clock.today()
    var currentDateIndex by mutableStateOf(dates.indexOf(searchDate))
    Button(onClick = {
        currentDateIndex--
        onDateSelected(dates[currentDateIndex])
    }, enabled = enabled && currentDateIndex > 0) {
        Text("<")
    }
    Text(
        text = searchDate?.prettyPrint(today.year) ?: "-",
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.onBackground,
        modifier = Modifier.width(100.dp),
    )
    Button(onClick = {
        currentDateIndex++
        onDateSelected(dates[currentDateIndex])
    }, enabled = enabled && currentDateIndex < (dates.size - 1)) {
        Text(">")
    }
}
