package seepick.localsportsclub.view.venue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.venue.detail.VenueDetail

@Composable
fun VenueScreen(
    viewModel: VenueViewModel = koinViewModel()
) {
    Column {
        Row {
            VenueSearchPanel()
        }
        Row(Modifier.weight(1.0f, true)) {
            VenuesTable()
            Spacer(Modifier.width(10.dp))
            VenueDetail(
                selectedVenue = viewModel.selectedVenue,
                editModel = viewModel.venueEdit,
                onUpdateVenue = viewModel::updateVenue,
            )
        }
    }
}

