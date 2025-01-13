package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import org.koin.compose.koinInject
import seepick.localsportsclub.api.UscConfig
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.CheckboxText
import seepick.localsportsclub.view.common.LabeledText
import seepick.localsportsclub.view.common.Lsc
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.common.NotesTextField
import seepick.localsportsclub.view.common.RatingPanel
import seepick.localsportsclub.view.common.TitleText
import seepick.localsportsclub.view.common.UrlTextField
import seepick.localsportsclub.view.shared.SimpleActivitiesTable
import seepick.localsportsclub.view.shared.SimpleFreetrainingsTable
import seepick.localsportsclub.view.venue.VenueImage

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
    uscConfig: UscConfig = koinInject(),
) {
    Column(Modifier.fillMaxWidth(1.0f).then(modifier)) {
        TitleText(venue.name, textDecoration = if (venue.isDeleted) TextDecoration.LineThrough else null)
        Row {
            VenueImage(venue.imageFileName)
            Column {
                if (venue.categories.isNotEmpty()) {
                    Text(venue.categories.joinToString(", "))
                }
                RatingPanel(venueEdit.rating)
            }
        }
        LabeledText("Description", venue.description)
        if (venue.city != uscConfig.city) LabeledText("City", venue.city.label)
        venue.importantInfo?.also { LabeledText("Info", it) }
        venue.openingTimes?.also { LabeledText("Times", it) }
        Row {
            CheckboxText("Favorited", venueEdit.isFavorited, Icons.Lsc.Favorites)
            CheckboxText("Wishlisted", venueEdit.isWishlisted, Icons.Lsc.Wishlists)
            CheckboxText("Hidden ${LscIcons.hidden}", venueEdit.isHidden)
        }
        UrlTextField(
            label = "Venue Site",
            url = venueEdit.officialWebsite.value,
            onChange = { venueEdit.officialWebsite.value = it },
            modifier = Modifier.fillMaxWidth()
        )
        UrlTextField(label = "USC Site", url = venue.uscWebsite, modifier = Modifier.fillMaxWidth())
        val (notes, notesSetter) = venueEdit.notes
        NotesTextField(notes = notes, setter = notesSetter)
        SimpleActivitiesTable(
            activities = venue.activities,
            selectedActivity = activity,
            onActivityClicked = onActivityClicked,
            modifier = Modifier.weight(0.5f),
        )
        SimpleFreetrainingsTable(
            freetrainings = venue.freetrainings,
            selectedFreetraining = freetraining,
            onFreetrainingClicked = onFreetrainingClicked,
            modifier = Modifier.weight(0.5f),
        )
        Button(
            onClick = onUpdateVenue,
            enabled = !venueEdit.isClean(),
        ) { Text("Update") }
    }
}
