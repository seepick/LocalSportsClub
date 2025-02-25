package seepick.localsportsclub.view

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
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.view.common.LabeledText
import seepick.localsportsclub.view.common.LscVScroll

@Composable
fun StatsDialog(
    onClose: () -> Unit,
    viewModel: StatsViewModel = koinInject(),
    clock: Clock = koinInject(),
) {
    val values = viewModel.values
    Dialog(
        onDismissRequest = {
            println("onDismissRequest")
        },
        properties = DialogProperties(
            dismissOnClickOutside = true,
        )
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier,
        ) {
            Column(
                Modifier.width(500.dp).height(500.dp)
                    .padding(20.dp)
            ) {
                val scrollState = rememberScrollState()
                Box(Modifier.weight(1.0f, true).fillMaxWidth(1.0f)) {
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        Text(
                            "Statistics", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        LabeledText("Total Checkins", values.totalCheckins.toString())
                        Spacer(Modifier.height(5.dp))
                        Text("Penalties:", fontWeight = FontWeight.Bold)
                        val currentYear = clock.now().year
                        values.penalties.forEach {
                            Text("${it.state.iconStringAndSuffix()}${it.name} @ ${it.from.prettyPrint(currentYear)}")
                        }
                        Spacer(Modifier.height(5.dp))
                        Text("Top Categories:", fontWeight = FontWeight.Bold)
                        values.topCategories.forEach {
                            Text("${it.category}: ${it.checkinsCount}")
                        }
                        Spacer(Modifier.height(5.dp))
                        Text("Venues Monthly Limit:", fontWeight = FontWeight.Bold)
                        values.venueCheckins.forEach {
                            Text("${it.venue.name}: ${it.checkinsCount}/${values.maxVenueCheckins ?: "?"}")
                        }
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
