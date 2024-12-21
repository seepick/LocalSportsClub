package seepick.localsportsclub.view.venue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.api.domain.Rating
import seepick.localsportsclub.view.common.Tooltip

@Composable
fun VenuesDetail(
    viewModel: VenueViewModel = koinViewModel(),
) {
    val venue = viewModel.selectedVenue
    // https://medium.com/@anandgaur22/jetpack-compose-chapter-7-forms-and-user-input-in-compose-f2ce3e355356
    Column(Modifier.width(300.dp)) {
        val uriHandler = LocalUriHandler.current

        Text(
            text = venue?.name ?: "N/A",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        VenueImage(venue?.id)
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

        RatingPanel()

//        TextField(value = "foo", {}, label = { Text("Label") })
        val (notes, notesSetter) = viewModel.venueEdit.notes
        NotesTextField(notes, notesSetter)
        Button(
            { viewModel.updateVenue() },
            enabled = viewModel.selectedVenue != null
        ) { Text("Update") }
    }
}

@Composable
fun NotesTextField(notes: String, setter: (String) -> Unit) {
    OutlinedTextField(
        label = { Text("Notes") },
        maxLines = 4,
        value = notes,
        onValueChange = setter,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun RatingPanel(
    viewModel: VenueViewModel = koinViewModel(),
) {

    Column {
        var ratingText = viewModel.venueEdit.rating.string
        var isMenuExpanded by remember { mutableStateOf(false) }
        var textFieldSize by remember { mutableStateOf(Size.Zero) }
        val icon = if (isMenuExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

        OutlinedTextField(
            value = ratingText,
            onValueChange = { /* no-op */ },
            readOnly = true,
            modifier = Modifier
                .width(150.dp)
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                },
            label = { Text("Rating") },
            trailingIcon = {
                Icon(icon, null, Modifier.clickable { isMenuExpanded = !isMenuExpanded })
            },
        )
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
        ) {
            Rating.entries.forEach { rating ->
                DropdownMenuItem(onClick = {
                    viewModel.venueEdit.rating = rating
                    isMenuExpanded = false
                }) {
                    Text(text = rating.string)
                }
            }
        }
    }
}
