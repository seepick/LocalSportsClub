package seepick.localsportsclub.view.shared

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.search.AbstractSearch
import seepick.localsportsclub.view.venue.detail.VenueDetail

@Composable
fun <ITEM : HasVenue, SEARCH : AbstractSearch<ITEM>> ScreenTemplate(
    searchPanel: @Composable () -> Unit,
    table: @Composable () -> Unit,
    viewModel: ScreenViewModel<ITEM, SEARCH>,
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
                val tableScrollState = rememberLazyListState()
                LazyColumn(state = tableScrollState, modifier = Modifier.width(400.dp)) {
                    item {
                        if (selectedVenue != null) {
                            VenueDetail(
                                venue = selectedVenue!!,
                                activity = selectedActivity,
                                freetraining = selectedFreetraining,
                                venueEdit = viewModel.venueEdit,
                                onUpdateVenue = viewModel::updateVenue,
                                onActivityClicked = viewModel::onActivitySelected,
                                onFreetrainingClicked = viewModel::onFreetrainingSelected,
                            )
                        }
                        selectedSubEntity?.also {
                            SubEntityDetail(
                                subEntity = it,
                                onBook = viewModel::onBook,
                                onCancelBooking = viewModel::onCancelBooking,
                                isBookingOrCancelInProgress = viewModel.isBookingOrCancelInProgress,
                                bookingDialog = viewModel.bookingDialog,
                                onCloseDialog = viewModel::onCloseBookingDialog,
                            )
                        }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(), adapter = rememberScrollbarAdapter(
                        scrollState = tableScrollState
                    )
                )
            }
        }
    }
}
