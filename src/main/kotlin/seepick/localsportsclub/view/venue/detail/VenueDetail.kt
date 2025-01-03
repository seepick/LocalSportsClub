package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import seepick.localsportsclub.service.Clock
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.CheckboxText
import seepick.localsportsclub.view.common.RatingPanel
import seepick.localsportsclub.view.common.TitleText
import seepick.localsportsclub.view.common.UrlTextField
import seepick.localsportsclub.view.venue.VenueImage

private val imageWidth = 300.dp
private val imageHeight = 200.dp

@Composable
fun VenueDetail(
    selectedVenue: Venue?,
    selectedActivity: Activity?,
    editModel: VenueEditModel,
    onUpdateVenue: () -> Unit,
    onSubActivityClicked: (Activity) -> Unit,
    clock: Clock = koinInject(),
    modifier: Modifier = Modifier,
) {
    val currentYear by remember { mutableStateOf(clock.today().year) }
    Column(Modifier.fillMaxWidth(1.0f).then(modifier)) {
        TitleText(selectedVenue?.name ?: "N/A")
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

        CheckboxText("Favorited", selectedVenue != null, editModel.isFavorited, Icons.Lsc.Favorites)
        CheckboxText("Wishlisted", selectedVenue != null, editModel.isWishlisted, Icons.Lsc.Wishlists)
        CheckboxText("Hidden", selectedVenue != null, editModel.isHidden)

        RatingPanel(enabled = selectedVenue != null, editModel.rating)

        UrlTextField(
            label = "Venue Site",
            url = editModel.officialWebsite.value,
            enabled = selectedVenue != null,
            onChange = { editModel.officialWebsite.value = it },
            modifier = Modifier.fillMaxWidth()
        )
        UrlTextField(
            label = "USC Site",
            url = selectedVenue?.uscWebsite,
            enabled = selectedVenue != null,
            modifier = Modifier.fillMaxWidth()
        )

        val (notes, notesSetter) = editModel.notes
        NotesTextField(selectedVenue != null, notes, notesSetter)

        selectedVenue?.activities?.also {
            SimpleActivitiesTable(it, selectedActivity = selectedActivity, onActivityClicked = onSubActivityClicked)
        }
        selectedVenue?.freetrainings?.also {
            // FIXME also react to when this one is clicked; make it a proper table internally
            SimpleFreetrainingsTable(it, currentYear)
        }

        Button(
            onClick = onUpdateVenue,
            enabled = selectedVenue != null && !editModel.isClean(),
        ) { Text("Update") }
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
