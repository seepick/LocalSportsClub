package seepick.localsportsclub.view.activity

import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.search.AbstractSearch

class ActivitySearch(allCategories: List<String>, resetItems: () -> Unit) : AbstractSearch<Activity>(resetItems) {
    val hidden = newBooleanSearchOption(
        "hidden", initiallyEnabled = true, initialValue = false
    ) { it.venue.isHidden }

    val name = newStringSearchOption(
        "Name", initiallyEnabled = true,
        extractors = listOf({ it.name }, { it.teacher }, { it.venue.name })
    )
    val date = newDateTimeRangeSearchOption(
        "Date", visualIndicator = Lsc.icons.dateIndicator
    ) { it.dateTimeRange }
    val booked = newBooleanSearchOption(
        "Booked", initialValue = true, visualIndicator = Lsc.icons.reservedEmojiIndicator
    ) { it.state == ActivityState.Booked }
    val favorited = newBooleanSearchOption(
        "Favorited", initialValue = true, visualIndicator = Lsc.icons.favoritedIndicator
    ) { it.venue.isFavorited }
    val wishlisted = newBooleanSearchOption(
        "Wishlisted", initialValue = true, visualIndicator = Lsc.icons.wishlistedIndicator
    ) { it.venue.isWishlisted }
    val rating = newRatingSearchOption(
        "Rating", visualIndicator = Lsc.icons.ratingIndicator
    ) { it.venue.rating }
    val categories = newSelectSearchOption(
        "Category", allOptions = allCategories, visualIndicator = Lsc.icons.categoryIndicator
    ) { listOf(it.category) }
}
