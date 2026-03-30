package seepick.localsportsclub.view.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.github.seepick.uscclient.plan.Plan
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.service.model.HasDistance
import seepick.localsportsclub.service.model.HasPlan
import seepick.localsportsclub.service.model.HasVenue
import seepick.localsportsclub.service.search.AbstractSearch
import seepick.localsportsclub.service.search.ComparingNumericComparator
import seepick.localsportsclub.service.search.RatingSearchOption
import seepick.localsportsclub.service.search.SelectSearchOption
import seepick.localsportsclub.view.common.VisualIndicator

fun <E : HasVenue> AbstractSearch<E>.newFavoritedSearchOption() = newBooleanSearchOption(
    label = "Favorited",
    initialValue = true,
    visualIndicator = Lsc.icons.favoritedIndicator,
    extractor = { it.venue.isFavorited },
)

fun <E : HasVenue> AbstractSearch<E>.newWishlistedSearchOption() = newBooleanSearchOption(
    label = "Wishlisted",
    initialValue = true,
    visualIndicator = Lsc.icons.wishlistedIndicator,
    extractor = { it.venue.isWishlisted },
)

fun <E : HasDistance> AbstractSearch<E>.newDistanceSearchOption() = newDoubleSearchOption(
    label = "Distance",
    initialValue = 1.0,
    initialComparator = ComparingNumericComparator.Lower,
    visualIndicator = Lsc.icons.distanceIndicator,
    extractor = { it.distanceInKm },
)

fun <T : HasVenue> AbstractSearch<T>.newRatingSearchOption(
    label: String = "Rating",
    initiallyEnabled: Boolean = false,
) = RatingSearchOption<T>(
    label = label,
    reset = ::reset,
    extractor = { it.venue.rating },
    initiallyEnabled = initiallyEnabled,
    visualIndicator = Lsc.icons.ratingIndicator,
).also {
    options += it
}

@Composable
fun <T : HasPlan> PlanSearchField(searchOption: SelectSearchOption<T>) {
    SelectSearchField(searchOption, width = 130.dp)
}

fun <T : HasPlan> AbstractSearch<T>.newPlanSearchOption() = newSelectSearchOption(
    visualIndicator = VisualIndicator.EmojiIndicator(Plan.UscPlan.emoji),
    label = "Plan",
    allOptions = Plan.UscPlan.entries.map { it.fullLabel },
    extractor = { listOf(it.plan.fullLabel) },
)
