package seepick.localsportsclub.view.preferences

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.Country
import seepick.localsportsclub.service.model.Credentials
import seepick.localsportsclub.service.model.Gcal
import seepick.localsportsclub.service.model.Preferences

class PreferencesViewEntity {
    var uscUsername: String by mutableStateOf("")
    var uscPassword: String by mutableStateOf("")
    var periodFirstDay: Int? by mutableStateOf(null)
    var country: Country? by mutableStateOf(null)
    var city: City? by mutableStateOf(null)
    var calendarEnabled: Boolean by mutableStateOf(false)
    var calendarId: String by mutableStateOf("")
    var homeLatitude: Double? by mutableStateOf(null)
    var homeLongitude: Double? by mutableStateOf(null)

    private lateinit var prefs: Preferences

    fun setup(prefs: Preferences) {
        this.prefs = prefs
        uscUsername = prefs.uscCredentials?.username ?: ""
        uscPassword = prefs.uscCredentials?.password ?: ""
        periodFirstDay = prefs.periodFirstDay
        country = prefs.city?.let { Country.byCityId(it.id) }
        city = prefs.city
        calendarEnabled = prefs.gcal is Gcal.GcalEnabled
        calendarId = prefs.gcal.maybeCalendarId ?: ""
        homeLatitude = prefs.home?.latitude
        homeLongitude = prefs.home?.longitude
    }

    fun isDirty() =
        prefs.city != city ||
                prefs.gcal != buildGcal() ||
                prefs.uscCredentials != buildCredentials() ||
                prefs.home != buildHomeLocation() ||
                prefs.periodFirstDay != periodFirstDay

    fun buildPreferences() = Preferences(
        uscCredentials = buildCredentials(),
        city = city,
        gcal = buildGcal(),
        home = buildHomeLocation(),
        periodFirstDay = periodFirstDay,
    )

    private fun buildGcal() =
        if (calendarEnabled && calendarId.isNotEmpty()) Gcal.GcalEnabled(calendarId) else Gcal.GcalDisabled

    fun buildCredentials() = if (uscUsername.isEmpty() || uscPassword.isEmpty()) null else Credentials(
        username = uscUsername, password = uscPassword,
    )

    private fun buildHomeLocation() = if (homeLatitude != null && homeLongitude != null) Location(
        latitude = homeLatitude!!,
        longitude = homeLongitude!!
    ) else null
}
