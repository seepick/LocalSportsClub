package seepick.localsportsclub.view.venue

import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.search.AbstractSearch

class VenueSearch(resetItems: () -> Unit) : AbstractSearch<Venue>(resetItems) {
    val name = newStringSearchOption(
        label = "Name",
        extractors = listOf { it.name },
        initiallyEnabled = true,
    )
}
