package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.CheckboxText
import seepick.localsportsclub.view.common.NotesTextField
import seepick.localsportsclub.view.common.RatingPanel
import seepick.localsportsclub.view.common.TitleText
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.UrlTextField
import seepick.localsportsclub.view.shared.SimpleActivitiesTable
import seepick.localsportsclub.view.shared.SimpleFreetrainingsTable
import seepick.localsportsclub.view.venue.VenueImage

@Composable
fun LabeledText(label: String, text: String) {
    Row {
        Text("$label:", fontWeight = FontWeight.Bold)
        Tooltip(text) {
            Text(text, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun VenueDetail(
    venue: Venue,
    activity: Activity?,
    freetraining: Freetraining?,
    venueEdit: VenueEditModel,
    onUpdateVenue: () -> Unit,
    onActivityClicked: ((Activity) -> Unit)?,
    onFreetrainingClicked: ((Freetraining) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(Modifier.fillMaxWidth(1.0f).then(modifier)) {
        TitleText(venue.name, textDecoration = if (venue.isDeleted) TextDecoration.LineThrough else null)
        VenueImage(venue.imageFileName)
        LabeledText("Facilities", venue.facilities.joinToString(", "))
        LabeledText("Description", venue.description)
        venue.importantInfo?.also { LabeledText("Info", it) }
        venue.openingTimes?.also { LabeledText("Times", it) }
        CheckboxText("Favorited", venueEdit.isFavorited, Icons.Lsc.Favorites)
        CheckboxText("Wishlisted", venueEdit.isWishlisted, Icons.Lsc.Wishlists)
        CheckboxText("Hidden", venueEdit.isHidden)
        RatingPanel(venueEdit.rating)
        UrlTextField(
            label = "Venue Site", url = venueEdit.officialWebsite.value,
            onChange = { venueEdit.officialWebsite.value = it }, modifier = Modifier.fillMaxWidth()
        )
        UrlTextField(label = "USC Site", url = venue.uscWebsite, modifier = Modifier.fillMaxWidth())
        val (notes, notesSetter) = venueEdit.notes
        NotesTextField(notes = notes, setter = notesSetter)
        SimpleActivitiesTable(
            activities = venue.activities,
            selectedActivity = activity,
            onActivityClicked = onActivityClicked,
        )
        SimpleFreetrainingsTable(
            freetrainings = venue.freetrainings,
            selectedFreetraining = freetraining,
            onFreetrainingClicked = onFreetrainingClicked,
        )
        Button(
            onClick = onUpdateVenue,
            enabled = !venueEdit.isClean(),
        ) { Text("Update") }
    }
}
