package seepick.localsportsclub.view.freetraining

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.search.BooleanSearchField
import seepick.localsportsclub.view.search.DateSearchField
import seepick.localsportsclub.view.search.RatingSearchField
import seepick.localsportsclub.view.search.SelectSearchField
import seepick.localsportsclub.view.search.StringSearchField

@Composable
fun FreetrainingSearchPanel(
    viewModel: FreetrainingViewModel = koinViewModel(),
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StringSearchField(viewModel.searching.name)
        DateSearchField(viewModel.searching.date, viewModel.syncDates)
        SelectSearchField(viewModel.searching.category)
        RatingSearchField(viewModel.searching.rating)
        BooleanSearchField(viewModel.searching.scheduled)
        BooleanSearchField(viewModel.searching.favorited)
        BooleanSearchField(viewModel.searching.wishlisted)
    }
}
