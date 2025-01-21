package seepick.localsportsclub.service.model

import seepick.localsportsclub.service.Location

data class Preferences(
    val uscCredentials: Credentials?,
    val city: City?,
    val gcal: Gcal,
    val home: Location?,
    val periodFirstDay: Int?,
) {
    companion object {
        val empty = Preferences(null, null, Gcal.GcalDisabled, null, null)
    }
}

sealed interface Gcal {
    val maybeCalendarId: String?

    data object GcalDisabled : Gcal {
        override val maybeCalendarId = null
    }

    data class GcalEnabled(val calendarId: String) : Gcal {
        override val maybeCalendarId = calendarId
    }
}
