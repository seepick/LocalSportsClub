package seepick.localsportsclub.view.activity

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.search.ComparingNumericComparator
import seepick.localsportsclub.view.search.BooleanSearchField
import seepick.localsportsclub.view.search.DateTimeRangeSearchField
import seepick.localsportsclub.view.search.DoubleSearchField
import seepick.localsportsclub.view.search.GenericSearchPanel
import seepick.localsportsclub.view.search.PlanSearchField
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
        PlanSearchField(viewModel.searching.plan)
        RatingSearchField(viewModel.searching.rating, tooltip = "Venue Rating")
        DoubleSearchField(viewModel.searching.distance, ComparingNumericComparator.entries)
        SelectSearchField(viewModel.searching.categories)

        BooleanSearchField(viewModel.searching.favorited)
        BooleanSearchField(viewModel.searching.wishlisted)
        BooleanSearchField(viewModel.searching.booked)

        RemarkRatingSearchField(viewModel.searching.activityRating, tooltip = "Activity Remark Rating")
        RemarkRatingSearchField(viewModel.searching.teacherRating, tooltip = "Teacher Remark Rating")
    }
}
