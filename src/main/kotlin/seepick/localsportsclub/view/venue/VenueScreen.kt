package seepick.localsportsclub.view.venue

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.MainViewModel
import seepick.localsportsclub.view.shared.ScreenTemplate

@Composable
fun VenueScreen(
    viewModel: VenueViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
) {
    ScreenTemplate(
        searchPanel = { VenueSearchPanel() },
        table = { VenuesTable() },
        viewModel = viewModel,
        mainViewModel = mainViewModel,
    )
}
