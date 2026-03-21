package seepick.localsportsclub.usage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.seepick.uscclient.shared.daysBetween
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.view.common.LabeledText
import seepick.localsportsclub.view.common.LscVScroll
import seepick.localsportsclub.view.common.Tooltip
import java.time.LocalDate
import kotlin.math.abs

@Composable
fun UsageStatsDialog(
    onClose: () -> Unit,
    viewModel: UsageStatsViewModel = koinInject(),
    clock: Clock = koinInject(),
) {
    val values = viewModel.values
    Dialog(
        onDismissRequest = {
            println("onDismissRequest")
        }, properties = DialogProperties(
            dismissOnClickOutside = true,
        )
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier,
        ) {
            Column(
                Modifier.width(500.dp).height(500.dp).padding(20.dp)
            ) {
                val scrollState = rememberScrollState()
                Box(Modifier.weight(1.0f, true).fillMaxWidth(1.0f)) {
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        Text(
                            "Usage Statistics",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Text("This Month's Visits:", fontWeight = FontWeight.Bold)
                        values.monthlyVenueCheckins.forEach {
                            SelectionContainer {
                                Text("${it.venue.name}: ${it.checkinsCount}/${it.maxCheckinsMonth ?: "?"}")
                            }
                        }
                        Spacer(Modifier.height(5.dp))
                        Text("Recent Penalties:", fontWeight = FontWeight.Bold)
                        val currentYear = clock.now().year
                        values.penalties.forEach {
                            Tooltip(it.state.label) {
                                SelectionContainer {
                                    Text(
                                        "${it.state.iconStringAndSuffix()}${it.name} @ ${it.from.prettyPrint(currentYear)}"
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(5.dp))
                        Text("Top Categories:", fontWeight = FontWeight.Bold)
                        values.topCategories.forEach {
                            SelectionContainer {
                                Text("${it.checkinsCount}x ${it.category.nameAndMaybeEmoji}")
                            }
                        }
                        Spacer(Modifier.height(5.dp))
                        Text("Top Venues:", fontWeight = FontWeight.Bold)
                        values.topVenues.forEach {
                            SelectionContainer {
                                Text("${it.venue.name}: ${it.checkinsCount}")
                            }
                        }
                        Spacer(Modifier.height(5.dp))
                        LabeledText("Total Checkins", values.totalCheckins.toString())
                        Spacer(Modifier.height(5.dp))
                        LabeledText("Active For", renderDaysNicely(values.firstCheckinDate, clock.today()))
                    }
                    LscVScroll(rememberScrollbarAdapter(scrollState))
                }
                Row(modifier = Modifier.fillMaxWidth(1.0f)) {
                    Spacer(Modifier.weight(1.0f))
                    Button(onClick = onClose, modifier = Modifier.align(Alignment.Bottom)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

fun renderDaysNicely(start: LocalDate, today: LocalDate): String {
    val years = today.year - start.year
    val days = abs(today.daysBetween(start.withYear(today.year)))
    return buildString {
        if (years > 0) {
            append("$years year")
            append(if (years == 1) "" else "s")
            append(" and ")
        }
        append("$days days")
    }
}
