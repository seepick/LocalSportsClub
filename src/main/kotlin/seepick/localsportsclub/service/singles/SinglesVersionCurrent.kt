package seepick.localsportsclub.service.singles

import kotlinx.serialization.Serializable
import seepick.localsportsclub.service.LocalDateTimeSerializer
import java.time.LocalDateTime

typealias CityId = Int

// not yet deployed to PROD (running v1). still ok to do changes in the current version.

// do NOT add/edit/delete anything here; requires manual migration!
@Serializable
data class SinglesVersionCurrent(
    val notes: String?,
    val lastSyncs: Map<CityId, @Serializable(with = LocalDateTimeSerializer::class) LocalDateTime>,
    val windowWidth: Int?,
    val windowHeight: Int?,
    val windowPosX: Int?,
    val windowPosY: Int?,
    var planInternalId: String?,
    val prefUscCredUsername: String?,
    val prefUscCredPassword: String?,
    val prefCityId: Int?,
    val prefGoogleCalendarId: String?,
    val prefHomeLat: Double?,
    val prefHomeLong: Double?,
    val prefPeriodFirstDay: Int?,
) {
    companion object {
        val VERSION = 2
        val empty = SinglesVersionCurrent(
            notes = null,
            lastSyncs = emptyMap(),
            windowWidth = null,
            windowHeight = null,
            windowPosX = null,
            windowPosY = null,
            planInternalId = null,
            prefUscCredUsername = null,
            prefUscCredPassword = null,
            prefCityId = null,
            prefGoogleCalendarId = null,
            prefHomeLat = null,
            prefHomeLong = null,
            prefPeriodFirstDay = null,
        )
    }

    override fun toString() =
        "SinglesVersionCurrent[lastSyncs=$lastSyncs,planInternalId=$planInternalId,prefCityId=$prefCityId,...]"
}

@Serializable
data class SinglesVersionV1(
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
        val empty = SinglesVersionV1(
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

    override fun toString() = "SinglesVersionV1[lastSync=$lastSync,planApiString=$planApiString,prefCityId=$prefCityId]"
}
