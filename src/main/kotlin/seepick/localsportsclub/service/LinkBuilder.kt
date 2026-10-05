package seepick.localsportsclub.service

import com.github.seepick.uscclient.baseUrl
import com.github.seepick.uscclient.model.UscLang
import seepick.localsportsclub.service.date.machinePrint
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.Venue

object LinkBuilder {
    private const val SERVICE_TYPE_ACTIVITY = 0
    private const val SERVICE_TYPE_FREE_TRAINING = 1

    fun buildVenueLink(venue: Venue) = "${UscLang.English.baseUrl}venues/${venue.venue.slug}"

    /**
     * @return e.g.: "https://urbansportsclub.com/en/venues/double-shift?date=2026-10-07&service_type=1"
     */
    fun buildVenueLink(activity: Activity) = "${UscLang.English.baseUrl}venues/${activity.venue.slug}?" +
            "date=${activity.dateTimeRange.from.toLocalDate().machinePrint()}&" +
            "service_type=$SERVICE_TYPE_ACTIVITY"


    fun buildVenueLink(freetraining: Freetraining) =
        "${UscLang.English.baseUrl}venues/${freetraining.venue.slug}?" +
                "date=${freetraining.date.machinePrint()}&" +
                "service_type=$SERVICE_TYPE_FREE_TRAINING"
}
