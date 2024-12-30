package seepick.localsportsclub.view.venue

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.common.ScreenTemplate

@Composable
fun VenueScreen(
    viewModel: VenueViewModel = koinViewModel()
) {
    ScreenTemplate(
        searchPanel = { VenueSearchPanel() },
        table = { VenuesTable() },
        viewModel = viewModel,
    )
}
