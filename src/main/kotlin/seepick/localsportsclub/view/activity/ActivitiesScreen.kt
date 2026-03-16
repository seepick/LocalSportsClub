package seepick.localsportsclub.view.activity

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.MainViewModel
import seepick.localsportsclub.view.remark.RemarkViewModel
import seepick.localsportsclub.view.shared.ScreenTemplate

@Composable
fun ActivitiesScreen(
    viewModel: ActivityViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel(),
    remarkViewModel: RemarkViewModel = koinViewModel(),
) {
    ScreenTemplate(
        searchPanel = { ActivitySearchPanel() },
        table = { ActivitiesTable() },
        viewModel = viewModel,
        mainViewModel = mainViewModel,
        remarkViewModel = remarkViewModel,
    )
}
