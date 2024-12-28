package seepick.localsportsclub.view.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
    Column {
        Row {
            ActivitySearchPanel()
        }
        Row(Modifier.weight(1.0f, true)) {
            ActivitiesTable()
            Spacer(Modifier.width(10.dp))
            VenueDetail(
                selectedVenue = selectedVenue,
                editModel = viewModel.venueEdit,
                onUpdateVenue = viewModel::updateVenue,
            )
        }
    }
}
