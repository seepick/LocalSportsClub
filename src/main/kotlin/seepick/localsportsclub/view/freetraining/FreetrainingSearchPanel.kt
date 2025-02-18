package seepick.localsportsclub.view.freetraining

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.search.BooleanSearchField
import seepick.localsportsclub.view.search.DateSearchField
import seepick.localsportsclub.view.search.GenericSearchPanel
import seepick.localsportsclub.view.search.RatingSearchField
import seepick.localsportsclub.view.search.SelectSearchField
import seepick.localsportsclub.view.search.StringSearchField

@Composable
fun FreetrainingSearchPanel(
    viewModel: FreetrainingViewModel = koinViewModel(),
) {
    GenericSearchPanel(
        clearSearchEnabled = viewModel.searching.anyEnabled,
        clearSearch = viewModel.searching::clearAll,
    ) {
        StringSearchField(viewModel.searching.name)
        DateSearchField(viewModel.searching.date, viewModel.syncDates)
        BooleanSearchField(viewModel.searching.scheduled)
        BooleanSearchField(viewModel.searching.favorited)
        BooleanSearchField(viewModel.searching.wishlisted)
        RatingSearchField(viewModel.searching.rating)
        SelectSearchField(viewModel.searching.category)
    }
}
