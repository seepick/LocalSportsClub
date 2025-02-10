package seepick.localsportsclub.view.activity

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.search.BooleanSearchField
import seepick.localsportsclub.view.search.DateTimeRangeSearchField
import seepick.localsportsclub.view.search.GenericSearchPanel
import seepick.localsportsclub.view.search.RatingSearchField
import seepick.localsportsclub.view.search.SelectSearchField
import seepick.localsportsclub.view.search.StringSearchField

@Composable
fun ActivitySearchPanel(
    viewModel: ActivityViewModel = koinViewModel(),
) {
    GenericSearchPanel {
        StringSearchField(viewModel.searching.name)
        DateTimeRangeSearchField(viewModel.searching.date, viewModel.syncDates)
        BooleanSearchField(viewModel.searching.booked)
        BooleanSearchField(viewModel.searching.favorited)
        BooleanSearchField(viewModel.searching.wishlisted)
        RatingSearchField(viewModel.searching.rating)
        SelectSearchField(viewModel.searching.categories)
    }
}
