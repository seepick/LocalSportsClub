package seepick.localsportsclub.view.venue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
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
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.Clock
import seepick.localsportsclub.service.model.Rating
import seepick.localsportsclub.view.common.Tooltip

private val imageWidth = 300.dp
private val imageHeight = 200.dp

@Composable
fun VenueDetail(
    viewModel: VenueViewModel = koinViewModel(),
    clock: Clock = koinInject()
) {
    val currentYear by remember { mutableStateOf(clock.today().year) }

    val venue = viewModel.selectedVenue
    // https://medium.com/@anandgaur22/jetpack-compose-chapter-7-forms-and-user-input-in-compose-f2ce3e355356
    Column(Modifier.width(300.dp)) {
        val uriHandler = LocalUriHandler.current
        /*
            val isFavorited: Boolean,
            val isWishlisted: Boolean,
            val isHidden: Boolean,
            ...
         */
        Text(
            text = venue?.name ?: "N/A",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colors.primary,
        )
        if (venue == null) {
            Spacer(Modifier.height(imageHeight))
        } else {
            Row(modifier = Modifier.width(imageWidth).height(imageHeight)) {
                VenueImage(venue.imageFileName)
            }
        }
        Text("Facilities: ${venue?.facilities?.joinToString(", ") ?: ""}")
        Row {
            Text("Description:", fontWeight = FontWeight.Bold)
            Text(venue?.description ?: "", fontSize = 10.sp, maxLines = 2)
        }
        venue?.importantInfo?.also { info ->
            Row {
                Text("Info:", fontWeight = FontWeight.Bold)
                Text(info, fontSize = 10.sp, maxLines = 2)
            }
        }
        venue?.openingTimes?.also { times ->
            Row {
                Text("Times:", fontWeight = FontWeight.Bold)
                Text(times, fontSize = 10.sp, maxLines = 2)
            }
        }

        Row {
            Checkbox(viewModel.venueEdit.isFavorited, { viewModel.venueEdit.isFavorited = it })
            Text("Favorited")
        }

        RatingPanel()

        Tooltip(venue?.uscWebsite?.toString()) {
            Button({
                uriHandler.openUri(venue?.uscWebsite?.toString()!!)
            }, enabled = venue?.uscWebsite != null) {
                Text("Open USC Website")
            }
        }
        Tooltip(venue?.officialWebsite?.toString()) {
            Button({
                uriHandler.openUri(venue?.officialWebsite?.toString()!!)
            }, enabled = venue?.officialWebsite != null) {
                Text("Open Official Website")
            }
        }


//        TextField(value = "foo", {}, label = { Text("Label") })
        val (notes, notesSetter) = viewModel.venueEdit.notes
        NotesTextField(notes, notesSetter)

        LazyColumn {
            items(viewModel.selectedVenue?.activities ?: emptyList()) { activity ->
                Row {
                    if (activity.scheduled) {
                        Text(text = "⭐️")
                    }
                    Text(text = "${activity.name} - ${activity.fromToFormatted(currentYear)}")
                }
            }
        }

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
        var isMenuExpanded by remember { mutableStateOf(false) }
        var textFieldSize by remember { mutableStateOf(Size.Zero) }
        val icon = if (isMenuExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

        OutlinedTextField(
            value = viewModel.venueEdit.rating.string,
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
