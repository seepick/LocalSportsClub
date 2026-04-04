package seepick.localsportsclub.view.activity

import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.Category
import seepick.localsportsclub.service.search.AbstractSearch
import seepick.localsportsclub.service.search.SearchOpt
import seepick.localsportsclub.view.GlobalKeyboard
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.search.newDistanceSearchOption
import seepick.localsportsclub.view.search.newFavoritedSearchOption
import seepick.localsportsclub.view.search.newPlanSearchOption
import seepick.localsportsclub.view.search.newRatingSearchOption
import seepick.localsportsclub.view.search.newWishlistedSearchOption

class ActivitySearch(
    allCategories: List<Category>,
    resetItems: () -> Unit,
    globalKeyboard: GlobalKeyboard,
) : AbstractSearch<Activity>(globalKeyboard, resetItems) {
    val hidden = newBooleanSearchOption( // invisible to the user
        label = "hidden",
        initiallyEnabled = true,
        initialValue = false,
        extractor = { it.venue.isHidden }
    )
    val activityNameTeacherAndVenue = newStringSearchOption(
        label = "Search",
        initiallyEnabled = true,
        extractors = listOf(
            { it.name },
            { it.teacher },
            { it.venue.name },
        )
    )
    val date = newDateTimeRangeSearchOption(
        "Date", visualIndicator = Lsc.icons.dateIndicator
    ) { it.dateTimeRange }
    val booked = newBooleanSearchOption(
        "Booked", initialValue = true, visualIndicator = Lsc.icons.reservedIndicator
    ) { it.state == ActivityState.Booked }
    val distance = newDistanceSearchOption()
    val favorited = newFavoritedSearchOption()
    val wishlisted = newWishlistedSearchOption()
    val rating = newRatingSearchOption(label = "Venue")
    val activityRating = newRemarkRatingSearchOption(
        "Activity", visualIndicator = Lsc.icons.activitiesIndicator
    ) { it.remark?.rating }
    val teacherRating = newRemarkRatingSearchOption(
        "Teacher", visualIndicator = Lsc.icons.teachersIndicator
    ) { it.teacherRemark?.rating }

    val categories = newSelectSearchOption(
        label = "Category",
        visualIndicator = Lsc.icons.categoryIndicator,
        allOptions = allCategories.map { it.toSearchOpt() },
        extractor = { listOf(it.category.toSearchOpt()) },
    )
    val plan = newPlanSearchOption()
}

private fun Category.toSearchOpt() =
    SearchOpt(renderedLabel = nameAndEmojiAndActivityCount, compareValue = name)
