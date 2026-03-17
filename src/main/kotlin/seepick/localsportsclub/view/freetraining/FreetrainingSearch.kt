package seepick.localsportsclub.view.freetraining

import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.model.Category
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.search.AbstractSearch
import java.time.LocalDate

class FreetrainingSearch(
    allCategories: List<Category>,
    searchDates: List<LocalDate>,
    resetItems: () -> Unit,
) : AbstractSearch<Freetraining>(resetItems) {
    val hidden = newBooleanSearchOption(
        "hidden", initiallyEnabled = true, initialValue = false
    ) { it.venue.isHidden }

    val name = newStringSearchOption(
        "Freetraining/Venue", initiallyEnabled = true, extractors = listOf({ it.name }, { it.venue.name })
    )
    val date = newDateSearchOption(
        "Date", initialDate = searchDates.first(), visualIndicator = Lsc.icons.dateIndicator
    ) { it.date }
    val scheduled = newBooleanSearchOption(
        "Scheduled", initialValue = true, visualIndicator = Lsc.icons.reservedIndicator
    ) { it.state == FreetrainingState.Scheduled }
    val favorited = newBooleanSearchOption(
        "Favorited", initialValue = true, visualIndicator = Lsc.icons.favoritedIndicator
    ) { it.venue.isFavorited }
    val wishlisted = newBooleanSearchOption(
        "Wishlisted", initialValue = true, visualIndicator = Lsc.icons.wishlistedIndicator
    ) { it.venue.isWishlisted }
    val rating = newRatingSearchOption(
        "Rating", visualIndicator = Lsc.icons.ratingIndicator
    ) { it.venue.rating }
    val category = newSelectSearchOption(
        label = "Category",
        allOptions = allCategories.map { it.nameAndMaybeEmoji },
        visualIndicator = Lsc.icons.categoryIndicator,
        extractor = { listOf(it.category.nameAndMaybeEmoji) },
    )
}
