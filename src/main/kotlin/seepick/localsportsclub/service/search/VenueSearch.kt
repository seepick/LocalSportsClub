package seepick.localsportsclub.service.search

import seepick.localsportsclub.service.model.Venue

class VenueSearch(resetItems: () -> Unit) : AbstractSearch<Venue>(resetItems) {
    val name = newStringSearchOption(
        label = "Name",
        extractors = listOf { it.name },
        initiallyEnabled = true,
    )
}
