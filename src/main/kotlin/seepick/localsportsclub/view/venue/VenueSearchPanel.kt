package seepick.localsportsclub.view.venue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.search.BooleanSearchField
import seepick.localsportsclub.view.search.IntSearchField
import seepick.localsportsclub.view.search.SelectSearchField
import seepick.localsportsclub.view.search.StringSearchField

@Composable
fun VenueSearchPanel(
    viewModel: VenueViewModel = koinViewModel(),
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StringSearchField(viewModel.searching.name)
        BooleanSearchField(viewModel.searching.wishlisted)
        BooleanSearchField(viewModel.searching.favorited)
        BooleanSearchField(viewModel.searching.hidden)
        IntSearchField(viewModel.searching.checkins)
        IntSearchField(viewModel.searching.activities)
        IntSearchField(viewModel.searching.bookings)
        SelectSearchField(viewModel.searching.category)
    }
}
