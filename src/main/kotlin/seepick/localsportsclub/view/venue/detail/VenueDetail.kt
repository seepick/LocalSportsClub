package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import seepick.localsportsclub.MainWindowState
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.CheckboxText
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.LabeledText
import seepick.localsportsclub.view.common.Lsc
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.common.NotesTextField
import seepick.localsportsclub.view.common.RatingPanel
import seepick.localsportsclub.view.common.TitleText
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.UrlText
import seepick.localsportsclub.view.common.UrlTextField
import seepick.localsportsclub.view.common.WidthOrFill
import seepick.localsportsclub.view.shared.SimpleActivitiesTable
import seepick.localsportsclub.view.shared.SimpleActivitiesTable_rowEstimatedHeight
import seepick.localsportsclub.view.shared.SimpleFreetrainingsTable
import seepick.localsportsclub.view.shared.SimpleFreetrainingsTable_rowEstimatedHeight
import seepick.localsportsclub.view.venue.VenueImage
import java.net.URLEncoder
import kotlin.math.max
import kotlin.math.min

@Composable
fun VenueDetail(
    venue: Venue,
    activity: Activity?,
    freetraining: Freetraining?,
    venueEdit: VenueEditModel,
    onUpdateVenue: () -> Unit,
    onActivityClicked: ((Activity) -> Unit)?,
    showLinkedVenues: Boolean,
    onVenueSelected: (Venue) -> Unit,
    onFreetrainingClicked: ((Freetraining) -> Unit)?,
    modifier: Modifier = Modifier,
    reducedVSpace: Boolean,
    configuredCity: City?,
    mainWindowState: MainWindowState = koinInject(),
) {
    val uriHandler = LocalUriHandler.current
    Column(Modifier.fillMaxWidth(1.0f).then(modifier)) {
        TitleText(venue.name, textDecoration = if (venue.isDeleted) TextDecoration.LineThrough else null)
        Row {
            Box(modifier = Modifier.width(200.dp)) {
                VenueImage(venue.imageFileName)
            }
            Spacer(Modifier.width(5.dp))
            Column {
                if (venue.categories.isNotEmpty()) {
                    Text(venue.categories.joinToString(", "))
                }
                Row(verticalAlignment = Alignment.Bottom) {
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
                            text = " ${distance}km away",
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
                RatingPanel(venueEdit.rating.value, { venueEdit.rating.value = it })
            }
        }

        Tooltip(venue.description) {
            Text(venue.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (venue.city != configuredCity) {
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
        NotesTextField(
            notes = notes, setter = notesSetter, modifier = Modifier.heightIn(min = 500.dp).weight(1f)
        )
        Button(
            onClick = onUpdateVenue,
            enabled = !venueEdit.isClean(),
        ) { Text("Update") }

        if (showLinkedVenues && venue.linkedVenues.isNotEmpty()) {
            DropDownTextField(
                label = "Select Linked Venue",
                items = venue.linkedVenues,
                onItemSelected = { onVenueSelected(it!!) },
                itemFormatter = { it?.name ?: "" },
                selectedItem = null as Venue?,
                textSize = WidthOrFill.FillWidth,
            )
        }
        val heights = calcTableHeights(
            reducedVSpace = reducedVSpace,
            windowHeight = mainWindowState.height,
            activitiesCount = venue.activities.size,
            freetrainingsCount = venue.freetrainings.size,
        )
        SimpleActivitiesTable(
            activities = venue.activities,
            selectedActivity = activity,
            onActivitySelected = onActivityClicked,
            modifier = Modifier.fillMaxWidth(),
            height = heights.first,
        )
        SimpleFreetrainingsTable(
            freetrainings = venue.freetrainings,
            selectedFreetraining = freetraining,
            onFreetrainingSelected = onFreetrainingClicked,
            modifier = Modifier.fillMaxWidth(),
            height = heights.second,
        )
    }
}

private fun calcTableHeights(
    reducedVSpace: Boolean, windowHeight: Int, activitiesCount: Int, freetrainingsCount: Int
): Pair<Dp, Dp> {
    val maxActivityRows = 20
    val maxFreetrainingRows = 5
    val activitiesCalcRows = calcRows(
        reducedVSpace, 4, maxActivityRows, SimpleActivitiesTable_rowEstimatedHeight, windowHeight
    )
    val freetrainingsCalcRows = calcRows(
        reducedVSpace, 2, maxFreetrainingRows, SimpleFreetrainingsTable_rowEstimatedHeight, windowHeight
    )
    val activityLeftovers = if (activitiesCount < activitiesCalcRows) activitiesCalcRows - activitiesCount else 0
    val freetrainingLeftovers =
        if (freetrainingsCount < freetrainingsCalcRows) freetrainingsCalcRows - freetrainingsCount else 0
    return SimpleActivitiesTable_rowEstimatedHeight.dp * min(
        activitiesCalcRows + freetrainingLeftovers, activitiesCount
    ) to SimpleFreetrainingsTable_rowEstimatedHeight.dp * min(
        freetrainingsCalcRows + activityLeftovers, freetrainingsCount
    )
}

private fun calcRows(reducedVSpace: Boolean, min: Int, max: Int, rowEstimatedHeight: Int, windowHeight: Int): Int {
    val gap = 850 + (if (reducedVSpace) 150 else 0)
    val calced = (windowHeight - gap) / rowEstimatedHeight
    return min(max, max(min, calced))
}
