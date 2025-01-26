package seepick.localsportsclub.view.activity

import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.search.AbstractSearch

class ActivitySearch(allCategories: List<String>, resetItems: () -> Unit) : AbstractSearch<Activity>(resetItems) {
    val hidden = newBooleanSearchOption("hidden", initiallyEnabled = true, initialValue = false) { it.venue.isHidden }
    val name = newStringSearchOption(
        "Activity/Venue", initiallyEnabled = true,
        extractors = listOf({ it.name }, { it.teacher }, { it.venue.name })
    )
    val date = newDateTimeRangeSearchOption("Date") { it.dateTimeRange }
    val booked = newBooleanSearchOption("Booked", initialValue = true) { it.state == ActivityState.Booked }
    val favorited = newBooleanSearchOption("Favorited", initialValue = true) { it.venue.isFavorited }
    val wishlisted = newBooleanSearchOption("Wishlisted", initialValue = true) { it.venue.isWishlisted }
    val rating = newRatingSearchOption("Rating") { it.venue.rating }
    val categories = newSelectSearchOption("Category", allOptions = allCategories) { listOf(it.category) }
}
