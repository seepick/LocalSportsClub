package seepick.localsportsclub.view.freetraining

import seepick.localsportsclub.view.GlobalKeyboard
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.service.model.Category
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.search.AbstractSearch
import seepick.localsportsclub.view.search.newRatingSearchOption
import java.time.LocalDate

class FreetrainingSearch(
    allCategories: List<Category>,
    searchDates: List<LocalDate>,
    resetItems: () -> Unit,
    globalKeyboard: GlobalKeyboard,
) : AbstractSearch<Freetraining>(globalKeyboard, resetItems) {
    val hidden = newBooleanSearchOption(
        "hidden", initiallyEnabled = true, initialValue = false
    ) { it.venue.isHidden }

    val name = newStringSearchOption(
        "Search", initiallyEnabled = true, extractors = listOf({ it.name }, { it.venue.name })
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
    val rating = newRatingSearchOption()
    val category = newSelectSearchOption(
        label = "Category",
        allOptions = allCategories.map { it.nameAndMaybeEmoji },
        visualIndicator = Lsc.icons.categoryIndicator,
        extractor = { listOf(it.category.nameAndMaybeEmoji) },
    )
}
