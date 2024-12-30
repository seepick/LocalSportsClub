package seepick.localsportsclub.view.common

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
import seepick.localsportsclub.view.activity.ActivityDetail
import seepick.localsportsclub.view.freetraining.FreetrainingDetail
import seepick.localsportsclub.view.venue.detail.VenueDetail

@Composable
fun <ITEM : ScreenItem, SEARCH : AbstractSearch<ITEM>> ScreenTemplate(
    searchPanel: @Composable () -> Unit,
    table: @Composable () -> Unit,
    viewModel: ScreenViewModel<ITEM, SEARCH>,
) {

    val selectedVenue by viewModel.selectedVenue.collectAsState()
    val selectedActivity by viewModel.selectedActivity.collectAsState()
    val selectedFreetraining by viewModel.selectedFreetraining.collectAsState()

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
                        VenueDetail(
                            selectedVenue = selectedVenue,
                            selectedActivity = selectedActivity,
                            selectedFreetraining = selectedFreetraining,
                            editModel = viewModel.venueEdit,
                            onUpdateVenue = viewModel::updateVenue,
                            onSubActivityClicked = viewModel::onActivitySelected,
                            onSubFreetrainingClicked = viewModel::onFreetrainingSelected,
                        )
                        ActivityDetail(activity = selectedActivity)
                        FreetrainingDetail(freetraining = selectedFreetraining)
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(
                        scrollState = tableScrollState
                    )
                )
            }
        }
    }
}
