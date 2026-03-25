package seepick.localsportsclub.view.venue

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.Category
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.search.AbstractSearch
import seepick.localsportsclub.service.search.FullNumericComparator
import seepick.localsportsclub.view.common.Lsc
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.search.newDistanceSearchOption
import seepick.localsportsclub.view.search.newFavoritedSearchOption
import seepick.localsportsclub.view.search.newPlanSearchOption
import seepick.localsportsclub.view.search.newRatingSearchOption
import seepick.localsportsclub.view.search.newWishlistedSearchOption
import java.time.LocalDate

class VenueSearch(
    today: LocalDate,
    allCategories: List<Category>,
    resetItems: () -> Unit,
) :
    AbstractSearch<Venue>(resetItems) {

    val name = newStringSearchOption(
        label = "Name", initiallyEnabled = true, extractors = listOf { it.name },
    )
    val activities = newIntSearchOption(
        label = "Activities",
        visualIndicator = Lsc.icons.activitiesIndicator,
        initialValue = 10,
        initialComparator = FullNumericComparator.Bigger,
    ) { it.activities.filter { !it.isInPast(today) }.size }
    val reservations = newIntSearchOption(
        label = "Reservations",
        visualIndicator = Lsc.icons.reservedIndicator,
        initialValue = 0,
        initialComparator = FullNumericComparator.Bigger,
    ) {
        it.activities.count { it.state == ActivityState.Booked } +
                it.freetrainings.count { it.state == FreetrainingState.Scheduled }
    }
    val checkins = newIntSearchOption(
        label = "Checkins",
        visualIndicator = Lsc.icons.checkedinIndicator,
        initialValue = 0,
        initialComparator = FullNumericComparator.Bigger,
    ) {
        it.activities.count { it.state == ActivityState.Checkedin } +
                it.freetrainings.count { it.state == FreetrainingState.Checkedin }
    }
    val hidden = newBooleanSearchOption(
        "Hidden", initialValue = true, visualIndicator = Lsc.icons.hiddenIndicator
    ) { it.isHidden }
    val distance = newDistanceSearchOption()
    val favorited = newFavoritedSearchOption()
    val wishlisted = newWishlistedSearchOption()
    val plan = newPlanSearchOption()
    val rating = newRatingSearchOption()
    val category = newSelectSearchOption(
        label = "Category",
        allOptions = allCategories.map { it.nameAndMaybeEmoji },
        visualIndicator = Lsc.icons.categoryIndicator,
        extractor = { venue -> venue.categories.map { cat -> cat.nameAndMaybeEmoji } },
    )
    val autoSync = newBooleanSearchOption(
        label = "Auto-Sync",
        initialValue = true,
        visualIndicator = VisualIndicator.VectorIndicator(Icons.Lsc.manualSync)
    ) { it.isAutoSync }
    val deleted = newBooleanSearchOption(
        label = "Deleted",
        initialValue = false,
        initiallyEnabled = true,
        visualIndicator = VisualIndicator.VectorIndicator(Icons.Default.Delete),
    ) { it.isDeleted }
}
