package seepick.localsportsclub.view.freetraining

import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.search.AbstractSearch
import java.time.LocalDate

class FreetrainingSearch(
    allCategories: List<String>,
    searchDates: List<LocalDate>,
    resetItems: () -> Unit,
) :
    AbstractSearch<Freetraining>(resetItems) {
    val hidden = newBooleanSearchOption("hidden", initiallyEnabled = true, initialValue = false) {
        it.venue.isHidden
    }
    val name = newStringSearchOption(
        "Freetraining/Venue", initiallyEnabled = true,
        extractors = listOf({ it.name }, { it.venue.name })
    )
    val date = newDateSearchOption("Date", initialDate = searchDates.first()) { it.date }
    val category = newSelectSearchOption("Category", allOptions = allCategories) { listOf(it.category) }
    val rating = newRatingSearchOption("Rating") { it.venue.rating }
    val favorited = newBooleanSearchOption("Favorited", initialValue = true) { it.venue.isFavorited }
    val wishlisted = newBooleanSearchOption("Wishlisted", initialValue = true) { it.venue.isWishlisted }
    val scheduled = newBooleanSearchOption("Scheduled", initialValue = true) { it.state == FreetrainingState.Scheduled }
}
