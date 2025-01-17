package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.UrlText
import seepick.localsportsclub.view.common.UrlTextField
import seepick.localsportsclub.view.shared.SimpleActivitiesTable
import seepick.localsportsclub.view.shared.SimpleFreetrainingsTable
import seepick.localsportsclub.view.venue.VenueImage
import java.net.URLEncoder

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
    val uriHandler = LocalUriHandler.current
    Column(Modifier.fillMaxWidth(1.0f).then(modifier)) {
        TitleText(venue.name, textDecoration = if (venue.isDeleted) TextDecoration.LineThrough else null)
        Row {
            VenueImage(venue.imageFileName)
            Spacer(Modifier.width(5.dp))
            Column {
                if (venue.categories.isNotEmpty()) {
                    Text(venue.categories.joinToString(", "))
                }

                Tooltip("Open Google Maps") {
                    UrlText(
                        url = "https://www.google.com/maps/search/?api=1&query=${
                            URLEncoder.encode(
                                "${venue.street}, ${venue.postalCode} ${venue.addressLocality}", "UTF-8"
                            )
                        }",
                        displayText = "${venue.street}${if (venue.street.isEmpty()) "" else ", "}${venue.postalCode}",
                    )
                }
                venue.distanceInKm?.also { distance ->
                    Text(
                        text = "${distance}km away",
                        fontSize = 10.sp,
                    )
                }

                RatingPanel(venueEdit.rating.value, { venueEdit.rating.value = it })
            }
        }
        Tooltip(venue.description) {
            Text(venue.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (venue.city != uscConfig.city) {
            Spacer(Modifier.height(5.dp))
            LabeledText("City", venue.city.label)
        }
        venue.importantInfo?.also {
            Spacer(Modifier.height(5.dp))
            LabeledText("Info", it)
        }
        venue.openingTimes?.also {
            Spacer(Modifier.height(5.dp))
            LabeledText("Times", it)
        }
        Row {
            CheckboxText("Favorited", venueEdit.isFavorited, Icons.Lsc.Favorites)
            CheckboxText("Wishlisted", venueEdit.isWishlisted, Icons.Lsc.Wishlists)
            CheckboxText("Hidden ${LscIcons.hidden}", venueEdit.isHidden)
        }
        Row {
            Tooltip(venue.uscWebsite) {
                Button(
                    onClick = { uriHandler.openUri(venue.uscWebsite) },
                    modifier = Modifier.height(56.dp),
                ) {
                    Text("USC Site")
                }
            }
            Spacer(Modifier.width(8.dp))
            UrlTextField(
                label = "Venue Site",
                url = venueEdit.officialWebsite.value,
                onChange = { venueEdit.officialWebsite.value = it },
                modifier = Modifier.weight(0.9f)
            )
        }
        val (notes, notesSetter) = venueEdit.notes
        NotesTextField(notes = notes, setter = notesSetter)
        Button(
            onClick = onUpdateVenue,
            enabled = !venueEdit.isClean(),
        ) { Text("Update") }

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
    }
}
