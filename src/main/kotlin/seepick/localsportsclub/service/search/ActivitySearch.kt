package seepick.localsportsclub.service.search

import seepick.localsportsclub.service.model.Activity

class ActivitySearch(resetItems: () -> Unit) : AbstractSearch<Activity>(resetItems) {
    val name = newStringSearchOption(
        "Activity/Venue Name", initiallyEnabled = true,
        extractors = listOf({ it.name }, { it.venue.name })
    )
    val date = newDateTimeRangeSearchOption("Date") { it.dateTimeRange }
    val booked = newBooleanSearchOption("Booked") { it.isBooked }
}
