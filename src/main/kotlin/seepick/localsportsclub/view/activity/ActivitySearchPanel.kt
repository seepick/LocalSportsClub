package seepick.localsportsclub.view.activity

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.search.BooleanSearchField
import seepick.localsportsclub.view.search.DateTimeRangeSearchField
import seepick.localsportsclub.view.search.StringSearchField

@Composable
fun ActivitySearchPanel(
    viewModel: ActivityViewModel = koinViewModel(),
) {
    Row {
        StringSearchField(viewModel.searching.name)
        DateTimeRangeSearchField(viewModel.searching.date)
        BooleanSearchField(viewModel.searching.booked)
        // TODO search for: fav, wish, booked, rating, category
    }
}
