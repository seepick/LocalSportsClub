package seepick.localsportsclub.view.search

import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.model.HasDistance
import seepick.localsportsclub.service.search.AbstractSearch
import seepick.localsportsclub.service.search.ComparingNumericComparator

fun <E : HasDistance> AbstractSearch<E>.newDistanceSearchOption() = newDoubleSearchOption(
    label = "Distance",
    initialValue = 1.0,
    initialComparator = ComparingNumericComparator.Lower,
    visualIndicator = Lsc.icons.distanceEmojiIndicator,
    extractor = { it.distanceInKm },
)
