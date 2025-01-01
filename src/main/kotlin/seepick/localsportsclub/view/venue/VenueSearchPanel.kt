package seepick.localsportsclub.view.venue

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.search.StringSearchField

@Composable
fun VenueSearchPanel(
    viewModel: VenueViewModel = koinViewModel(),
) {
    Row {
        StringSearchField(viewModel.searching.name)
        // TODO search for: fav, wish, rating, hidden, activities.count, checkins.count, bookings.count, category
    }
}
