package seepick.localsportsclub.view.activity

import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.search.AbstractSearch

class ActivitySearch(resetItems: () -> Unit) : AbstractSearch<Activity>(resetItems) {
    val hidden = newBooleanSearchOption("hidden", initiallyEnabled = true, initialValue = false) { it.venue.isHidden }
    val name = newStringSearchOption(
        "Activity/Venue Name", initiallyEnabled = true,
        extractors = listOf({ it.name }, { it.teacher }, { it.venue.name })
    )
    val date = newDateTimeRangeSearchOption("Date") { it.dateTimeRange }
    val booked = newBooleanSearchOption("Booked ✅") { it.state == ActivityState.Booked }
}
