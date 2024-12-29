package seepick.localsportsclub.view.search

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.service.Clock
import seepick.localsportsclub.service.DateTimeRange
import seepick.localsportsclub.service.SystemClock
import seepick.localsportsclub.service.parseTimeOrNull
import seepick.localsportsclub.service.prettyPrint
import seepick.localsportsclub.service.search.DateTimeRangeSearchOption
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
@Preview
        /*
        Caused by: java.lang.NoSuchMethodError: 'int[] kotlin.text.HexExtensionsKt.getBYTE_TO_LOWER_CASE_HEX_DIGITS()'
            at kotlin.uuid.UuidKt__UuidKt.formatBytesInto$UuidKt__UuidKt(Uuid.kt:435)
            at kotlin.uuid.UuidKt__UuidKt.access$formatBytesInto(Uuid.kt:1)
            at kotlin.uuid.Uuid.toString(Uuid.kt:131)
            at org.koin.mp.KoinPlatformToolsKt.generateId(KoinPlatformTools.kt:39)
            at org.koin.core.module.Module.<init>(Module.kt:45)
            at org.koin.dsl.ModuleDSLKt.module(ModuleDSL.kt:35)
            at org.koin.dsl.ModuleDSLKt.module$default(ModuleDSL.kt:33)
            at seepick.localsportsclub.view.search.DateTimeRangeSearchFieldKt.DateTimeRangeSearchField_Preview$lambda$2$lambda$1(DateTimeRangeSearchField.kt:49)
            at org.koin.dsl.KoinApplicationKt.koinApplication(KoinApplication.kt:33)
            at org.koin.dsl.KoinApplicationKt.koinApplication(KoinApplication.kt:49)
            at org.koin.compose.KoinApplicationKt.KoinApplication(KoinApplication.kt:113)
            at seepick.localsportsclub.view.search.DateTimeRangeSearchFieldKt.DateTimeRangeSearchField_Preview(DateTimeRangeSearchField.kt:48)
            ... 60 more
         */
fun DateTimeRangeSearchField_Preview() {
    KoinApplication(application = {
        modules(module {
            single { SystemClock } bind Clock::class
            single { UscConfig(syncDaysAhead = 3) }
        })
    }) {
        DateTimeRangeSearchField(
            searchOption = DateTimeRangeSearchOption<Any>(
                label = "Label",
                reset = {},
                extractor = { DateTimeRange(LocalDateTime.now(), LocalDateTime.now().plusHours(1)) },
            ),
        )
    }
}


@Composable
fun <T> DateTimeRangeSearchField(
    searchOption: DateTimeRangeSearchOption<T>,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(searchOption.label)
        Checkbox(checked = searchOption.enabled, onCheckedChange = { searchOption.updateEnabled(it) })
        DateSelector(
            enabled = searchOption.enabled,
            searchDate = searchOption.searchDate,
            onDateSelected = searchOption::updateSearchDate
        )
        TimeRangeSelector(enabled = searchOption.enabled, onTimeSelected = searchOption::updateSearchTimeStart)
        Text("-")
        TimeRangeSelector(enabled = searchOption.enabled, onTimeSelected = searchOption::updateSearchTimeEnd)
    }
}

@Composable
fun DateSelector(
    enabled: Boolean,
    searchDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    clock: Clock = koinInject(),
    uscConfig: UscConfig = koinInject(),
) {
    val today = clock.today()
    val dates = (0..<uscConfig.syncDaysAhead).map { today.plusDays(it.toLong()) }
    if (searchDate == null) {
        onDateSelected(dates.first())
    }
    var currentDateIndex by mutableStateOf(dates.indexOf(searchDate))
    Button(onClick = {
        currentDateIndex--
        onDateSelected(dates[currentDateIndex])
    }, enabled = enabled && currentDateIndex > 0) {
        Text("<")
    }
    TextField(
        value = searchDate?.prettyPrint(today.year) ?: "",
        onValueChange = {},
        singleLine = true,
        readOnly = true,
        enabled = enabled,
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        modifier = Modifier.width(130.dp),
    )
    Button(onClick = {
        currentDateIndex++
        onDateSelected(dates[currentDateIndex])
    }, enabled = enabled && currentDateIndex < (dates.size - 1)) {
        Text(">")
    }
}

private val timesAndStrings = (6..22).map {
    val time = LocalTime.of(it, 0)
    time to time.prettyPrint()
}

@Composable
fun TimeRangeSelector(
    enabled: Boolean,
    onTimeSelected: (LocalTime?) -> Unit,
) {
    var time: LocalTime? by remember { mutableStateOf(null) }
    var timeAsString by remember { mutableStateOf("") }

    var isMenuExpanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val icon = if (isMenuExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    Column {
        TextField(
            value = timeAsString,
            enabled = enabled,
            isError = if (timeAsString.isEmpty()) false else parseTimeOrNull(timeAsString) == null,
            onValueChange = { enteredString ->
                timeAsString = enteredString
                if (enteredString.isEmpty()) onTimeSelected(null)
                else parseTimeOrNull(enteredString)?.also { enteredTime ->
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
                            isMenuExpanded = !isMenuExpanded
                        }
                    } else it
                })
            },
        )
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
            modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
        ) {
            timesAndStrings.forEach { timeAndString ->
                DropdownMenuItem(onClick = {
                    time = timeAndString.first
                    timeAsString = timeAndString.second
                    onTimeSelected(timeAndString.first)
                    isMenuExpanded = false
                }) {
                    Text(text = timeAndString.second)
                }
            }
        }
    }
}
