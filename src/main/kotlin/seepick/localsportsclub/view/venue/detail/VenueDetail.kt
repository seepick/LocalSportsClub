package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.seepick.uscclient.model.City
import org.koin.compose.koinInject
import seepick.localsportsclub.MainWindowState
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.CheckboxTexted
import seepick.localsportsclub.view.common.CustomDialog
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.LinkTonalButton
import seepick.localsportsclub.view.common.LongText
import seepick.localsportsclub.view.common.Lsc
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.common.NotesTextField
import seepick.localsportsclub.view.common.RatingPanel
import seepick.localsportsclub.view.common.TitleText
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.UrlText
import seepick.localsportsclub.view.common.UrlTextField
import seepick.localsportsclub.view.common.WidthOrFill
import seepick.localsportsclub.view.common.table.TableNavigation
import seepick.localsportsclub.view.shared.SharedModel
import seepick.localsportsclub.view.venue.VenueImage
import java.net.URLEncoder
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun VenueDetail(
    venue: Venue,
    visitsModel: MonthlyVisitsModel,
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
    isSyncing: Boolean,
    configuredCity: City?,
    isSyncVenueInProgress: Boolean,
    onSyncVenue: () -> Unit,
    sharedModel: SharedModel = koinInject(),
    mainWindowState: MainWindowState = koinInject(),
    onActivityNavigated: (TableNavigation, Activity) -> Unit,
    onFreetrainingNavigated: (TableNavigation, Freetraining) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        Modifier.fillMaxWidth(1.0f).then(modifier),
    ) {
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
                FlowRow(verticalArrangement = Arrangement.Bottom) {
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
                    venue.distanceInKm.also { distance ->
                        Text(
                            text = " $distance km away",
                        )
                    }
                }
                RatingPanel(venueEdit.rating.value, { venueEdit.rating.value = it })
                MonthlyVisitsPanel(visitsModel)
            }
        }
        LongText(text = venue.description, onShowLongText = {
            sharedModel.customDialog.value =
                CustomDialog(title = "Description", text = it, showDismissButton = false)
        })
        if (venue.city != configuredCity) {
            Spacer(Modifier.height(5.dp))
            LongText(label = "City", text = venue.city.label)
        }
        venue.importantInfo?.also {
            Spacer(Modifier.height(5.dp))
            LongText(label = "Info", text = it, onShowLongText = {
                sharedModel.customDialog.value =
                    CustomDialog(title = "Important Info", text = it, showDismissButton = false)
            })
        }
        venue.openingTimes?.also {
            Spacer(Modifier.height(5.dp))
            LongText(label = "Times", text = it, onShowLongText = {
                sharedModel.customDialog.value =
                    CustomDialog(title = "Opening Times", text = it, showDismissButton = false)
            })
        }
        Row {
            CheckboxTexted("Favorited", venueEdit.isFavorited, images = Icons.Lsc.favorited2)
            CheckboxTexted("Wishlisted", venueEdit.isWishlisted, images = Icons.Lsc.wishlisted2)
            CheckboxTexted("Hidden ${LscIcons.hiddenEmoji}", venueEdit.isHidden, modifier = Modifier.height(30.dp))
            Spacer(Modifier.width(10.dp))
            CheckboxTexted(
                label = "Auto-Sync",
                tooltipText = "Automatically sync activity details on global sync",
                checked = venueEdit.isAutoSync,
                modifier = Modifier.height(30.dp),
                icon = Icons.Lsc.syncActivityDetails,
            )
        }
        Spacer(Modifier.height(2.dp))
        Row {
            Tooltip(venue.uscWebsite) {
                Button(
                    onClick = { uriHandler.openUri(venue.uscWebsite) },
                    modifier = Modifier.height(56.dp),
                ) {
                    LinkTonalButton(venue.uscWebsite)
                    Spacer(Modifier.width(5.dp))
                    Text("USC", modifier = Modifier.onClick { uriHandler.openUri(venue.uscWebsite) })
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
            enabled = !venueEdit.isClean() && !isSyncing,
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
            activities = venue.sortedActivities.toList(),
            selectedActivity = activity,
            onActivitySelected = onActivityClicked,
            onItemNavigation = onActivityNavigated,
            height = heights.first,
            modifier = Modifier.fillMaxWidth(),
            isSyncVenueInProgress = isSyncVenueInProgress,
            onSyncVenue = onSyncVenue,
        )
        SimpleFreetrainingsTable(
            freetrainings = venue.sortedFreetrainings.toList(),
            selectedFreetraining = freetraining,
            onFreetrainingSelected = onFreetrainingClicked,
            onItemNavigation = onFreetrainingNavigated,
            modifier = Modifier.fillMaxWidth(),
            height = heights.second,
        )
    }
}

private fun calcTableHeights(
    reducedVSpace: Boolean, windowHeight: Int, activitiesCount: Int, freetrainingsCount: Int,
): Pair<Dp, Dp> {
    val maxActivityRows = 20
    val maxFreetrainingRows = 5
    val activitiesCalcRows = calcRows(
        reducedVSpace, 3, maxActivityRows, SimpleActivitiesTable_rowEstimatedHeight, windowHeight
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
    val gap = 850 + (if (reducedVSpace) 120 else 0)
    val calced = (windowHeight - gap) / rowEstimatedHeight
    return min(max, max(min, calced))
}
