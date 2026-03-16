package seepick.localsportsclub.view.activity

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.search.ComparingNumericComparator
import seepick.localsportsclub.view.search.BooleanSearchField
import seepick.localsportsclub.view.search.DateTimeRangeSearchField
import seepick.localsportsclub.view.search.DoubleSearchField
import seepick.localsportsclub.view.search.GenericSearchPanel
import seepick.localsportsclub.view.search.RatingSearchField
import seepick.localsportsclub.view.search.RemarkRatingSearchField
import seepick.localsportsclub.view.search.SelectSearchField
import seepick.localsportsclub.view.search.StringSearchField

@Composable
fun ActivitySearchPanel(
    viewModel: ActivityViewModel = koinViewModel(),
) {
    GenericSearchPanel(
        clearSearchEnabled = viewModel.searching.anyEnabled,
        clearSearch = viewModel.searching::clearAll,
    ) {
        StringSearchField(viewModel.searching.activityNameTeacherAndVenue)
        DateTimeRangeSearchField(viewModel.searching.date, viewModel.syncDates, viewModel.timeRange)
        BooleanSearchField(viewModel.searching.booked)
        DoubleSearchField(viewModel.searching.distance, ComparingNumericComparator.entries)
        BooleanSearchField(viewModel.searching.favorited)
        BooleanSearchField(viewModel.searching.wishlisted)
        SelectSearchField(viewModel.searching.categories)
        SelectSearchField(viewModel.searching.plan, width = 120.dp)
        RatingSearchField(viewModel.searching.rating, tooltip = "Venue Rating")
        RemarkRatingSearchField(viewModel.searching.activityRating, tooltip = "Activity Remark Rating")
        RemarkRatingSearchField(viewModel.searching.teacherRating, tooltip = "Teacher Remark Rating")
    }
}
