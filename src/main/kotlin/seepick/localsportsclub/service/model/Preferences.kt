package seepick.localsportsclub.service.model

import seepick.localsportsclub.api.Credentials
import seepick.localsportsclub.service.Location

data class Preferences(
    val uscCredentials: Credentials?,
    val city: City?,
    val gcal: Gcal,
    val home: Location?,
)

sealed interface Gcal {
    val maybeCalendarId: String?

    data object GcalDisabled : Gcal {
        override val maybeCalendarId = null
    }

    data class GcalEnabled(val calendarId: String) : Gcal {
        override val maybeCalendarId = calendarId
    }
}
