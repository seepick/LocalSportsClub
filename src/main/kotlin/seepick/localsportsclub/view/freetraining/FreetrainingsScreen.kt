package seepick.localsportsclub.view.freetraining

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.common.ScreenTemplate
import seepick.localsportsclub.view.search.StringSearchField

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
    ScreenTemplate(
        searchPanel = { FreetrainingSearchPanel() },
        table = { FreetrainingsTable() },
        viewModel = viewModel,
    )
}

