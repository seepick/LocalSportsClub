package seepick.localsportsclub.view.venue

import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.search.AbstractSearch
import seepick.localsportsclub.view.search.newDistanceSearchOption

class VenueSearch(allCategories: List<String>, resetItems: () -> Unit) : AbstractSearch<Venue>(resetItems) {
    val name = newStringSearchOption(
        label = "Name", initiallyEnabled = true, extractors = listOf { it.name },
    )
    val distance = newDistanceSearchOption()
    val wishlisted = newBooleanSearchOption(
        "Wishlisted", initialValue = true, visualIndicator = Lsc.icons.wishlistedIndicator
    ) { it.isWishlisted }
    val favorited = newBooleanSearchOption(
        "Favorited", initialValue = true, visualIndicator = Lsc.icons.favoritedIndicator
    ) { it.isFavorited }
    val hidden = newBooleanSearchOption(
        "Hidden", initialValue = true, visualIndicator = Lsc.icons.hiddenIndicator
    ) { it.isHidden }
    val checkins = newIntSearchOption("Checkins", visualIndicator = Lsc.icons.checkedinIndicator) {
        it.activities.count { it.state == ActivityState.Checkedin } +
                it.freetrainings.count { it.state == FreetrainingState.Checkedin }
    }
    val activities = newIntSearchOption(
        "Activities", visualIndicator = Lsc.icons.activitiesIndicator
    ) { it.activities.size }
    val reservations = newIntSearchOption(
        "Reservations", visualIndicator = Lsc.icons.reservedEmojiIndicator
    ) {
        it.activities.count { it.state == ActivityState.Booked } +
                it.freetrainings.count { it.state == FreetrainingState.Scheduled }
    }
    val rating = newRatingSearchOption(
        "Rating", visualIndicator = Lsc.icons.ratingIndicator
    ) { it.rating }
    val category = newSelectSearchOption(
        "Category", allOptions = allCategories, visualIndicator = Lsc.icons.categoryIndicator
    ) { it.categories }
}
