package seepick.localsportsclub.view.freetraining

import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.search.AbstractSearch

class FreetrainingsSearch(resetItems: () -> Unit) : AbstractSearch<Freetraining>(resetItems) {
    val hidden = newBooleanSearchOption("hidden", initiallyEnabled = true, initialValue = false) {
        it.venue.isHidden
    }
    val name = newStringSearchOption(
        "Freetraining/Venue Name", initiallyEnabled = true,
        extractors = listOf({ it.name }, { it.venue.name })
    )
//    val date = newDateTimeRangeSearchOption("Date") { it.dateTimeRange }
//    val booked = newBooleanSearchOption("Booked") { it.isBooked }
}
