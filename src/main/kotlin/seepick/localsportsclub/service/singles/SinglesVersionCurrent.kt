package seepick.localsportsclub.service.singles

import kotlinx.serialization.Serializable
import seepick.localsportsclub.service.LocalDateTimeSerializer
import java.time.LocalDateTime

// do NOT add/edit/delete anything here; requires manual migration!
@Serializable
data class SinglesVersionCurrent(
    val notes: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val lastSync: LocalDateTime?,
    val windowWidth: Int?,
    val windowHeight: Int?,
    val windowPosX: Int?,
    val windowPosY: Int?,
    var planApiString: String?,
    val prefUscCredUsername: String?,
    val prefUscCredPassword: String?,
    val prefCityId: Int?,
    val prefGoogleCalendarId: String?,
    val prefHomeLat: Double?,
    val prefHomeLong: Double?,
    val prefPeriodFirstDay: Int?,
) {
    companion object {
        val VERSION = 1
        val empty = SinglesVersionCurrent(
            notes = null,
            lastSync = null,
            windowWidth = null,
            windowHeight = null,
            windowPosX = null,
            windowPosY = null,
            planApiString = null,
            prefUscCredUsername = null,
            prefUscCredPassword = null,
            prefCityId = null,
            prefGoogleCalendarId = null,
            prefHomeLat = null,
            prefHomeLong = null,
            prefPeriodFirstDay = null,
        )
    }
}
