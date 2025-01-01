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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.CheckboxText
import seepick.localsportsclub.view.common.NotesTextField
import seepick.localsportsclub.view.common.RatingPanel
import seepick.localsportsclub.view.common.TitleText
import seepick.localsportsclub.view.common.UrlTextField
import seepick.localsportsclub.view.shared.SimpleActivitiesTable
import seepick.localsportsclub.view.shared.SimpleFreetrainingsTable
import seepick.localsportsclub.view.venue.VenueImage

private val imageWidth = 300.dp
private val imageHeight = 200.dp

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

//           TODO: ensure max height! Row(modifier = Modifier.width(imageWidth).height(imageHeight)) {
        Row {
            VenueImage(venue.imageFileName)
        }

        Text("Facilities: ${venue.facilities.joinToString(", ")}")

        Row {
            Text("Description:", fontWeight = FontWeight.Bold)
            Text(venue.description, fontSize = 10.sp, maxLines = 2)
        }

        venue.importantInfo?.also { info ->
            Row {
                Text("Info:", fontWeight = FontWeight.Bold)
                Text(info, fontSize = 10.sp, maxLines = 2)
            }
        }

        venue.openingTimes?.also { times ->
            Row {
                Text("Times:", fontWeight = FontWeight.Bold)
                Text(times, fontSize = 10.sp, maxLines = 2)
            }
        }/*
        // TODO display all venue details in UI
        val slug: String,
        val cityId: Int,
        val postalCode: String,
        val street: String,
        val addressLocality: String,
        val latitude: String,
        val longitude: String,
        val isDeleted: Boolean,
         */

        CheckboxText("Favorited", venueEdit.isFavorited, Icons.Lsc.Favorites)
        CheckboxText("Wishlisted", venueEdit.isWishlisted, Icons.Lsc.Wishlists)
        CheckboxText("Hidden", venueEdit.isHidden)

        RatingPanel(venueEdit.rating)

        UrlTextField(
            label = "Venue Site",
            url = venueEdit.officialWebsite.value,
            onChange = { venueEdit.officialWebsite.value = it },
            modifier = Modifier.fillMaxWidth()
        )
        UrlTextField(
            label = "USC Site", url = venue.uscWebsite, modifier = Modifier.fillMaxWidth()
        )

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

