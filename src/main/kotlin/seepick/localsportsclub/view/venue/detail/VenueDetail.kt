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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.seepick.uscclient.model.City
import org.koin.compose.koinInject
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.MainWindowState
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.CheckboxTexted
import seepick.localsportsclub.view.common.CopyTextToClipboard
import seepick.localsportsclub.view.common.CustomDialog
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.LinkTonalButton
import seepick.localsportsclub.view.common.LongText
import seepick.localsportsclub.view.common.Lsc
import seepick.localsportsclub.view.common.NotesTextField
import seepick.localsportsclub.view.common.RatingPanel
import seepick.localsportsclub.view.common.SmallButton
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
    visitsModel: MonthlyVisitsModel?,
    activity: Activity?,
    freetraining: Freetraining?,
    venueEdit: VenueEditModel,
    onUpdateVenue: () -> Unit,
    activityRemarksCount: Int,
    teacherRemarksCount: Int,
    onViewActivityRemarks: () -> Unit,
    onViewTeacherRemarks: () -> Unit,
    onActivityClicked: ((Activity) -> Unit)?,
    showLinkedVenues: Boolean,
    onVenueSelected: (Venue) -> Unit,
    onFreetrainingClicked: ((Freetraining) -> Unit)?,
    modifier: Modifier = Modifier,
    reducedVSpace: Boolean,
    isSyncing: Boolean,
    configuredCity: City?,
    isSyncVenueActivitiesInProgress: Boolean,
    isSyncVenueDetailsInProgress: Boolean,
    onSyncVenueActivities: () -> Unit,
    onSyncVenueDetails: () -> Unit,
    sharedModel: SharedModel = koinInject(),
    mainWindowState: MainWindowState = koinInject(),
    clock: Clock = koinInject(),
    onActivityNavigated: (TableNavigation, Activity) -> Unit,
    onFreetrainingNavigated: (TableNavigation, Freetraining) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        Modifier.fillMaxWidth(1.0f).then(modifier),
    ) {
        TitleText(venue.name, textDecoration = if (venue.isDeleted) TextDecoration.LineThrough else null)
        Row(modifier = Modifier.height(113.dp)) { // enforce height to image's height
            Box(modifier = Modifier.width(200.dp)) {
                Tooltip("Click to open image carousel") {
                    VenueImage(venue, drawBorder = true)
                }
            }
            Spacer(Modifier.width(5.dp))
            Column {
                val text = buildString {
                    append(venue.plan.emoji)
                    if (venue.categories.isNotEmpty()) {
                        append(" | ")
                        append(venue.categories.joinToString(", ") { it.nameAndMaybeEmoji })
                    }
                }
                var isOverflowing by remember { mutableStateOf(false) }
                Tooltip(if (isOverflowing) text else null) {
                    SelectionContainer {
                        Text(
                            text = text,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { isOverflowing = it.hasVisualOverflow },
                        )
                    }
                }
                FlowRow(verticalArrangement = Arrangement.Bottom) {
                    Tooltip("Open Google Maps / Right click to copy") {
                        val displayText =
                            "${venue.street}${if (venue.street.isEmpty()) "" else ", "}${venue.postalCode}"
                        CopyTextToClipboard(text = displayText) {
                            UrlText(
                                url = "https://www.google.com/maps/search/?api=1&query=${
                                    URLEncoder.encode(
                                        "${venue.street}, ${venue.postalCode} ${venue.addressLocality}", "UTF-8"
                                    )
                                }",
                                displayText = displayText,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                        }
                    }
                    SelectionContainer {
                        Text("${venue.distanceInKm} km away")
                    }
                }
                visitsModel?.let { visits ->
                    MonthlyVisitsPanel(visits, modifier = Modifier.fillMaxWidth().height(20.dp))
                }
                LongText(
                    text = venue.description,
                    maxLines = null,
                    tooltip = "Click to open venue details dialog",
                    onShowLongText = {
                        sharedModel.customDialog.value = CustomDialog(
                            title = venue.name,
                            content = { VenueDetailDialogPanel(venue) },
                            showDismissButton = false,
                        )
                    },
                )
            }
        }
        Spacer(Modifier.height(5.dp))

        if (venue.city != configuredCity) {
            LongText(label = "City", text = venue.city.label, maxLines = 1)
            Spacer(Modifier.height(5.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Rating: ")
            RatingPanel(venueEdit.rating.value, { venueEdit.rating.value = it })
            Spacer(Modifier.width(20.dp))
            CheckboxTexted(
                icon = Icons.Lsc.favoritedIndicator,
                checked = venueEdit.isFavorited,
                tooltipText = "Mark as favorited",
                modifier = Modifier.height(30.dp),
            )
            Spacer(Modifier.width(10.dp))
            CheckboxTexted(
                icon = Lsc.icons.wishlistedIndicator,
                checked = venueEdit.isWishlisted,
                tooltipText = "Mark as wishlisted",
                modifier = Modifier.height(30.dp),
            )
            Spacer(Modifier.width(10.dp))
            CheckboxTexted(
                icon = Lsc.icons.hiddenIndicator,
                checked = venueEdit.isHidden,
                tooltipText = "Mark as hidden",
                modifier = Modifier.height(30.dp),
            )
            Spacer(Modifier.width(10.dp))
            CheckboxTexted(
                icon = Icons.Lsc.manualSyncIndicator,
                checked = venueEdit.isAutoSync,
                tooltipText = "Auto-sync activity details on global sync",
                modifier = Modifier.height(30.dp),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            val syncSize = 40
            Box(contentAlignment = Alignment.Center, modifier = Modifier.width(syncSize.dp).height(syncSize.dp)) {
                if (isSyncVenueDetailsInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size((syncSize - 15).dp))
                } else {
                    Tooltip("Sync venue details; last sync: ${venue.lastSync ?: "never"}\n(restart the application to display changes)") {
                        TextButton(onClick = onSyncVenueDetails) {
                            Icon(Lsc.icons.manualSync, contentDescription = null)
                        }
                    }
                }
            }
            Button(
                onClick = onUpdateVenue,
                enabled = !venueEdit.isClean() && !isSyncing,
            ) { Text("Update Venue") }
            Spacer(Modifier.width(16.dp))
            SmallButton(
                text = "Activity Remarks ($activityRemarksCount)",
                icon = Lsc.icons.activities,
                onClick = onViewActivityRemarks,
                tooltip = "Open activity remarks dialog ...",
            )
            Spacer(Modifier.width(5.dp))
            SmallButton(
                text = "Teacher Remarks ($teacherRemarksCount)",
                icon = Lsc.icons.teachers,
                onClick = onViewTeacherRemarks,
                tooltip = "Open teacher remarks dialog ...",
            )
        }

        if (showLinkedVenues && venue.linkedVenues.isNotEmpty()) {
            DropDownTextField(
                label = "Linked Venues",
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
            activities = venue.sortedActivities(clock.today()),
            selectedActivity = activity,
            onActivitySelected = onActivityClicked,
            onItemNavigation = onActivityNavigated,
            height = heights.first,
            modifier = Modifier.fillMaxWidth(),
            isSyncVenueInProgress = isSyncVenueActivitiesInProgress,
            onSyncVenue = onSyncVenueActivities,
        )
        SimpleFreetrainingsTable(
            freetrainings = venue.sortedFreetrainings(clock.today()),
            selectedFreetraining = freetraining,
            onFreetrainingSelected = onFreetrainingClicked,
            onItemNavigation = onFreetrainingNavigated,
            modifier = Modifier.fillMaxWidth(),
            height = heights.second,
        )
    }
}

@Composable
fun VenueDetailDialogPanel(venue: Venue) {
    Column {
        Text("Description", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        SelectionContainer {
            Text(venue.description)
        }
        venue.importantInfo?.also { info ->
            Spacer(Modifier.height(12.dp))
            Text("Info", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            SelectionContainer {
                Text(info)
            }
        }
        venue.openingTimes?.also { times ->
            Spacer(Modifier.height(12.dp))
            Text("Opening Times", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            SelectionContainer {
                Text(times)
            }
        }
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
