package seepick.localsportsclub.view.activity

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.shared.ScreenTemplate

@Composable
fun ActivitiesScreen(
    viewModel: ActivityViewModel = koinViewModel()
) {
    ScreenTemplate(
        searchPanel = { ActivitySearchPanel() },
        table = { ActivitiesTable() },
        viewModel = viewModel,
    )
}
