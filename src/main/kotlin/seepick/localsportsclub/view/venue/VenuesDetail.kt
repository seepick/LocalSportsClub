package seepick.localsportsclub.view.venue

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tooltip(text: String?, content: @Composable () -> Unit) {
    if (text == null) {
        content()
    } else {
        TooltipArea(
            tooltip = {
                Surface(
                    modifier = Modifier.shadow(4.dp), color = Color(255, 255, 210), shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = text, modifier = Modifier.padding(10.dp)
                    )
                }
            }, delayMillis = 600, tooltipPlacement = TooltipPlacement.CursorPoint(alignment = Alignment.BottomEnd)
        ) {
            content()
        }
    }
}

@Composable
fun VenuesDetail(
    viewModel: VenueViewModel = koinViewModel(),
) {
    val venue = viewModel.selectedVenue
    // https://medium.com/@anandgaur22/jetpack-compose-chapter-7-forms-and-user-input-in-compose-f2ce3e355356
    Column(Modifier.width(300.dp)) {
        val uriHandler = LocalUriHandler.current

        Text(venue?.name ?: "N/A")
        Text("Facilities: ${venue?.facilities ?: ""}")

        Tooltip(venue?.uscWebsite?.toString()) {
            Button({
                uriHandler.openUri(venue?.uscWebsite?.toString() ?: "")
            }, enabled = venue?.uscWebsite != null) {
                Text("Open USC Website")
            }
        }
        Tooltip(venue?.officialWebsite?.toString()) {
            Button({
                uriHandler.openUri(venue?.officialWebsite?.toString() ?: "")
            }, enabled = venue?.officialWebsite != null) {
                Text("Open Official Website")
            }
        }

        Text(venue?.rating?.string ?: "")
//        TextField(value = "foo", {}, label = { Text("Label") })
        OutlinedTextField(
            label = { Text("Notes") },
            maxLines = 4,
            value = viewModel.venueEdit.note,
            onValueChange = { viewModel.venueEdit.note = it },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            {
                viewModel.saveVenue()
            },
            enabled = viewModel.selectedVenue != null
        ) {
            Text("Save")
        }
    }
}
