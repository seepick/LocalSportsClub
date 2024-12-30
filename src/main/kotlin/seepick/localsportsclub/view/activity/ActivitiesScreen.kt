package seepick.localsportsclub.view.activity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.venue.detail.VenueDetail

@Composable
fun ActivitiesScreen(
    viewModel: ActivityViewModel = koinViewModel()
) {
    val selectedVenue by viewModel.selectedVenue.collectAsState()
    val selectedActivity by viewModel.selectedActivity.collectAsState()

    Column {
        ActivitySearchPanel()

        Row(Modifier.weight(1.0f, fill = true)) {
            Box(Modifier.weight(1.0f)) {
                ActivitiesTable()
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.width(400.dp)) {
                VenueDetail(
                    selectedVenue = selectedVenue,
                    selectedActivity = selectedActivity,
                    selectedFreetraining = null,
                    editModel = viewModel.venueEdit,
                    onUpdateVenue = viewModel::updateVenue,
                    onSubActivityClicked = viewModel::onActivityClicked,
                    onSubFreetrainingClicked = {},
                )
                ActivityDetail(
                    activity = selectedActivity,
                    modifier = Modifier.height(300.dp),
                )
            }
        }
    }
}
