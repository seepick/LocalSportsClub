package seepick.localsportsclub.view.freetraining

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.MainViewModel
import seepick.localsportsclub.view.shared.ScreenTemplate

@Composable
fun FreetrainingsScreen(
    viewModel: FreetrainingViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
) {
    ScreenTemplate(
        searchPanel = { FreetrainingSearchPanel() },
        table = { FreetrainingsTable() },
        viewModel = viewModel,
        mainViewModel = mainViewModel,
    )
}

