package seepick.localsportsclub.service.search

import seepick.localsportsclub.service.model.Activity

class ActivitySearch(resetItems: () -> Unit) : AbstractSearch<Activity>(resetItems) {
    val name = buildSearchOption("Activity/Venue Name", { it.name }, { it.venue.name })
}
