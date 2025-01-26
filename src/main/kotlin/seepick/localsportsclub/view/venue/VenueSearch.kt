package seepick.localsportsclub.view.venue

import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.search.AbstractSearch

class VenueSearch(allCategories: List<String>, resetItems: () -> Unit) : AbstractSearch<Venue>(resetItems) {
    val name = newStringSearchOption(
        label = "Name",
        extractors = listOf { it.name },
        initiallyEnabled = true,
    )
    val wishlisted = newBooleanSearchOption("Wishlisted", initialValue = true) { it.isWishlisted }
    val favorited = newBooleanSearchOption("Favorited", initialValue = true) { it.isFavorited }
    val hidden = newBooleanSearchOption("Hidden", initialValue = true) { it.isHidden }
    val checkins = newIntSearchOption("Checkins") {
        it.activities.count { it.state == ActivityState.Checkedin } +
                it.freetrainings.count { it.state == FreetrainingState.Checkedin }
    }
    val activities = newIntSearchOption("Activities") { it.activities.size }
    val bookings = newIntSearchOption("Bookings") {
        it.activities.count { it.state == ActivityState.Booked } +
                it.freetrainings.count { it.state == FreetrainingState.Scheduled }
    }
    val category = newSelectSearchOption("Category", allOptions = allCategories) {
        it.categories
    }
}
