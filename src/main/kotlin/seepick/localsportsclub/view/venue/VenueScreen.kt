package seepick.localsportsclub.view.venue

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.activity.ActivityDetail
import seepick.localsportsclub.view.venue.detail.VenueDetail

@Composable
fun VenueScreen(
    viewModel: VenueViewModel = koinViewModel()
) {
    Column {
        VenueSearchPanel()

        Row(Modifier.weight(1.0f, true)) {
            Box(Modifier.weight(1.0f)) {
                VenuesTable()
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.width(400.dp)) {
                VenueDetail(
                    selectedVenue = viewModel.selectedVenue,
                    selectedActivity = viewModel.selectedActivity,
                    selectedFreetraining = null,
                    editModel = viewModel.venueEdit,
                    onUpdateVenue = viewModel::updateVenue,
                    onSubActivityClicked = viewModel::onActivitySelected,
                    onSubFreetrainingClicked = null,
                )
                ActivityDetail(
                    activity = viewModel.selectedActivity,
                )
            }
        }
    }
}

