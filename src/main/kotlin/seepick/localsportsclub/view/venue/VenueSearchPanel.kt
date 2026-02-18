package seepick.localsportsclub.view.venue

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.search.ComparingNumericComparator
import seepick.localsportsclub.view.search.BooleanSearchField
import seepick.localsportsclub.view.search.DoubleSearchField
import seepick.localsportsclub.view.search.GenericSearchPanel
import seepick.localsportsclub.view.search.IntSearchField
import seepick.localsportsclub.view.search.RatingSearchField
import seepick.localsportsclub.view.search.SelectSearchField
import seepick.localsportsclub.view.search.StringSearchField

@Composable
fun VenueSearchPanel(
    viewModel: VenueViewModel = koinViewModel(),
) {
    GenericSearchPanel(
        clearSearchEnabled = viewModel.searching.anyEnabled,
        clearSearch = viewModel.searching::clearAll,
    ) {
        StringSearchField(viewModel.searching.name)
        IntSearchField(viewModel.searching.activities)
        IntSearchField(viewModel.searching.reservations)
        IntSearchField(viewModel.searching.checkins)
        BooleanSearchField(viewModel.searching.hidden)
        DoubleSearchField(viewModel.searching.distance, ComparingNumericComparator.entries)
        BooleanSearchField(viewModel.searching.favorited)
        BooleanSearchField(viewModel.searching.wishlisted)
        RatingSearchField(viewModel.searching.rating)
        SelectSearchField(viewModel.searching.category)
    }
}
