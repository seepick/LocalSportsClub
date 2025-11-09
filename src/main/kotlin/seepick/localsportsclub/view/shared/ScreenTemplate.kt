package seepick.localsportsclub.view.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.HasVenue
import seepick.localsportsclub.service.search.AbstractSearch
import seepick.localsportsclub.view.MainViewModel
import seepick.localsportsclub.view.venue.detail.VenueDetail
import seepick.localsportsclub.view.venue.detail.toMonthlyVisitsModel

@Composable
fun <ITEM : HasVenue, SEARCH : AbstractSearch<ITEM>> ScreenTemplate(
    searchPanel: @Composable () -> Unit,
    table: @Composable () -> Unit,
    viewModel: ScreenViewModel<ITEM, SEARCH>,
    mainViewModel: MainViewModel,
    clock: Clock = koinInject(),
) {
    val selectedVenue by viewModel.selectedVenue.collectAsState()
    val selectedSubEntity by viewModel.selectedSubEntity.collectAsState()
    val selectedActivity = selectedSubEntity?.maybeActivity
    val selectedFreetraining = selectedSubEntity?.maybeFreetraining

    Column {
        searchPanel()

        Row(Modifier.weight(1.0f, fill = true)) {
            Box(Modifier.weight(1.0f)) {
                table()
            }
            Spacer(Modifier.width(10.dp))
            Box {
//                val tableScrollState = rememberLazyListState()
                Column(
//                    state = tableScrollState,
                    modifier = Modifier.width(500.dp).fillMaxHeight(1.0f)
                ) {
//                    item {
                    if (selectedVenue != null) {
                        VenueDetail(
                            venue = selectedVenue!!,
                            visitsModel = selectedVenue!!.activities.toMonthlyVisitsModel(clock.today()),
                            activity = selectedActivity,
                            freetraining = selectedFreetraining,
                            onVenueSelected = viewModel::onVenueSelected,
                            onActivityClicked = viewModel::onActivitySelected,
                            onFreetrainingClicked = viewModel::onFreetrainingSelected,
                            onActivityNavigated = viewModel::onSubActivityNavigated,
                            onFreetrainingNavigated = viewModel::onSubFreetrainingNavigated,
                            venueEdit = viewModel.venueEdit,
                            onUpdateVenue = viewModel::updateVenue,
                            showLinkedVenues = viewModel.showLinkedVenues,
                            isSyncing = mainViewModel.isSyncing,
                            configuredCity = viewModel.configuredCity,
                            modifier = Modifier.weight(1.0f),
                            reducedVSpace = selectedSubEntity != null,
                        )
                    }
                    selectedSubEntity?.also {
                        SubEntityDetail(
                            subEntity = it,
                            onBook = viewModel::onBook,
                            onCancelBooking = viewModel::onCancelBooking,
                            syncActivityVisible = viewModel.syncActivityVisible,
                            onSyncActivity = viewModel::onSyncActivity,
                            isBookOrCancelPossible = viewModel.isBookOrCancelPossible && !mainViewModel.isSyncing,
                            isBookingOrCancelInProgress = viewModel.isBookingOrCancelInProgress,
                            onActivityChangeToCheckedin = viewModel::onActivityChangeToCheckedin,
                            isGcalEnabled = viewModel.isGcalEnabled,
                            shouldGcalBeManaged = viewModel.shouldGcalBeManaged,
                        )
                    }
                }
//                }
//                VerticalScrollbar(
//                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(), adapter = rememberScrollbarAdapter(
//                        scrollState = tableScrollState
//                    )
//                )
            }
        }
    }
}
