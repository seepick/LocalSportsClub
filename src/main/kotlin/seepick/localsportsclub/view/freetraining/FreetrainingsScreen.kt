package seepick.localsportsclub.view.freetraining

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
import seepick.localsportsclub.view.search.StringSearchField
import seepick.localsportsclub.view.venue.detail.VenueDetail

@Composable
fun FreetrainingSearchPanel(
    viewModel: FreetrainingViewModel = koinViewModel(),
) {
    Row {
        StringSearchField(viewModel.searching.name)
        // TODO date, category, rating, fav, wish
    }
}

@Composable
fun FreetrainingsScreen(
    viewModel: FreetrainingViewModel = koinViewModel()
) {
    val selectedVenue by viewModel.selectedVenue.collectAsState()
    val selectedFreetraining by viewModel.selectedFreetraining.collectAsState()

    Column {
        FreetrainingSearchPanel()
        Row(Modifier.weight(1.0f, fill = true)) {
            Box(Modifier.weight(1.0f)) {
                FreetrainingsTable()
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.width(400.dp)) {
                VenueDetail(
                    selectedVenue = selectedVenue,
                    selectedActivity = null,
                    selectedFreetraining = selectedFreetraining,
                    editModel = viewModel.venueEdit,
                    onUpdateVenue = viewModel::updateVenue,
                    onSubActivityClicked = {},
                    onSubFreetrainingClicked = viewModel::onFreetrainingClicked,
                )
                FreetrainingDetail(
                    freetraining = selectedFreetraining,
                    modifier = Modifier.height(300.dp),
                )
            }
        }
    }
}

