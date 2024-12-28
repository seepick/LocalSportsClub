package seepick.localsportsclub.view.venue

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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import seepick.localsportsclub.service.Clock
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.prettyPrint
import seepick.localsportsclub.service.prettyPrintWith
import seepick.localsportsclub.view.LscIcons
import seepick.localsportsclub.view.common.RatingPanel
import seepick.localsportsclub.view.common.UrlTextField

private val imageWidth = 300.dp
private val imageHeight = 200.dp

@Composable
fun VenueDetail(
    selectedVenue: Venue?,
    editModel: VenueEditModel,
    onUpdateVenue: () -> Unit,
    clock: Clock = koinInject(),
) {
    val currentYear by remember { mutableStateOf(clock.today().year) }
    Column(Modifier.width(300.dp)) {
        /*
            val isFavorited: Boolean,
            val isWishlisted: Boolean,
            val isHidden: Boolean,
            ...
         */
        Text(
            text = selectedVenue?.name ?: "N/A",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colors.primary,
        )
        if (selectedVenue == null) {
            Spacer(Modifier.height(imageHeight))
        } else {
            Row(modifier = Modifier.width(imageWidth).height(imageHeight)) {
                VenueImage(selectedVenue.imageFileName)
            }
        }
        Text("Facilities: ${selectedVenue?.facilities?.joinToString(", ") ?: ""}")
        Row {
            Text("Description:", fontWeight = FontWeight.Bold)
            Text(selectedVenue?.description ?: "", fontSize = 10.sp, maxLines = 2)
        }
        selectedVenue?.importantInfo?.also { info ->
            Row {
                Text("Info:", fontWeight = FontWeight.Bold)
                Text(info, fontSize = 10.sp, maxLines = 2)
            }
        }
        selectedVenue?.openingTimes?.also { times ->
            Row {
                Text("Times:", fontWeight = FontWeight.Bold)
                Text(times, fontSize = 10.sp, maxLines = 2)
            }
        }

        Row {
            Checkbox(
                checked = editModel.isFavorited,
                onCheckedChange = { editModel.isFavorited = it },
                enabled = selectedVenue != null,
            )
            Text("Favorited")
        }

        val (ratingGet, ratingSet) = editModel.rating
        RatingPanel(enabled = selectedVenue != null, ratingGet, ratingSet)

        UrlTextField(
            label = "Venue Site", url = selectedVenue?.officialWebsite, enabled = selectedVenue != null
        ) { selectedVenue?.officialWebsite = it }
        UrlTextField(label = "USC Site", url = selectedVenue?.uscWebsite, enabled = selectedVenue != null)

        val (notes, notesSetter) = editModel.notes
        NotesTextField(selectedVenue != null, notes, notesSetter)

        SimpleActivitiesTable(selectedVenue?.activities ?: emptyList(), currentYear)
        if (selectedVenue?.freetrainings?.isNotEmpty() == true) {
            Text("Freetrainings:")
            LazyColumn {
                items(selectedVenue?.freetrainings ?: emptyList()) { freetraining ->
                    Row {
                        if (freetraining.checkedinTime != null) {
                            Text(text = LscIcons.checkedin)
                        }
                        Text("${freetraining.name} / ${freetraining.category}: ")
                        Text(
                            text = if (freetraining.checkedinTime == null) {
                                freetraining.date.prettyPrint(currentYear)
                            } else {
                                freetraining.date.prettyPrintWith(freetraining.checkedinTime!!, currentYear)
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = onUpdateVenue,
            enabled = selectedVenue != null,
        ) { Text("Update") }
    }
}

@Composable
fun SimpleActivitiesTable(activities: List<Activity>, currentYear: Int) {
    if (activities.isEmpty()) {
        Text("No activities.")
    } else {
        Text("Activities:")
        LazyColumn {
            items(activities) { activity ->
                Row {
                    if (activity.isBooked) {
                        Text(text = LscIcons.booked)
                    }
                    if (activity.wasCheckedin) {
                        Text(text = LscIcons.checkedin)
                    }
                    Text(text = "${activity.name} - ${activity.dateTimeRange.prettyPrint(currentYear)}")
                }
            }
        }
    }
}

@Composable
fun NotesTextField(enabled: Boolean, notes: String, setter: (String) -> Unit) {
    OutlinedTextField(
        label = { Text("Notes") },
        maxLines = 4,
        value = notes,
        enabled = enabled,
        onValueChange = setter,
        modifier = Modifier.fillMaxWidth()
    )
}
