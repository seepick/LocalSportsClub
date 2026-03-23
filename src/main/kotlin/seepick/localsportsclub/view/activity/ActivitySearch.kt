package seepick.localsportsclub.view.activity

import com.github.seepick.uscclient.plan.Plan
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.Category
import seepick.localsportsclub.service.search.AbstractSearch
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.search.newDistanceSearchOption

class ActivitySearch(allCategories: List<Category>, resetItems: () -> Unit) : AbstractSearch<Activity>(resetItems) {
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
    val favorited = newBooleanSearchOption(
        label = "Favorited", initialValue = true, visualIndicator = Lsc.icons.favoritedIndicator,
    ) { it.venue.isFavorited }
    val wishlisted = newBooleanSearchOption(
        "Wishlisted", initialValue = true, visualIndicator = Lsc.icons.wishlistedIndicator
    ) { it.venue.isWishlisted }
    val rating = newRatingSearchOption(
        "Venue", visualIndicator = Lsc.icons.ratingIndicator
    ) { it.venue.rating }
    val activityRating = newRemarkRatingSearchOption(
        "Activity", visualIndicator = Lsc.icons.activitiesIndicator
    ) { it.remarkRating }
    val teacherRating = newRemarkRatingSearchOption(
        "Teacher", visualIndicator = Lsc.icons.teachersIndicator
    ) { it.teacherRemarkRating }

    val categories = newSelectSearchOption(
        visualIndicator = Lsc.icons.categoryIndicator,
        label = "Category",
        allOptions = allCategories.map { it.nameAndMaybeEmoji },
        extractor = { listOf(it.category.nameAndMaybeEmoji) },
    )
    val plan = newSelectSearchOption(
        visualIndicator = VisualIndicator.EmojiIndicator(Plan.UscPlan.emoji),
        label = "Plan",
        allOptions = Plan.UscPlan.entries.map { it.fullLabel },
        extractor = { listOf(it.plan.fullLabel) },
    )
}
