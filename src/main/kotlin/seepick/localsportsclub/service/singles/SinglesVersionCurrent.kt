package seepick.localsportsclub.service.singles

import kotlinx.serialization.Serializable
import seepick.localsportsclub.service.LocalDateTimeSerializer
import java.time.LocalDateTime

typealias CityId = Int


val SinglesVersionCurrent.Companion.empty
    get() = SinglesVersionCurrent(
        notes = null,
        lastSyncs = emptyMap(),
        windowWidth = null,
        windowHeight = null,
        windowPosX = null,
        windowPosY = null,
        planInternalId = null,
        prefUscCredentials = null,
        prefCityId = null,
        prefGoogleCalendarId = null,
        prefHomeLat = null,
        prefHomeLong = null,
        prefPeriodFirstDay = null,
        verifiedUscCredentials = null,
        verifiedGcalId = null,
    )

@Serializable
data class JsonCredentials(
    val username: String,
    val encryptedPassword: String,
) {
    override fun toString() = "JsonCredentials[username=$username,encryptedPassword=****]"
}

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
    val prefUscCredentials: JsonCredentials?,
    val prefCityId: Int?,
    val prefGoogleCalendarId: String?,
    val prefHomeLat: Double?,
    val prefHomeLong: Double?,
    val prefPeriodFirstDay: Int?,
    val verifiedUscCredentials: JsonCredentials?,
    val verifiedGcalId: String?,
) {
    companion object {
        const val VERSION = 3
    }

    override fun toString() =
        "SinglesVersionCurrent[lastSyncs=$lastSyncs,planInternalId=$planInternalId,prefCityId=$prefCityId,...]"
}

@Serializable
data class SinglesVersionV2(
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
        const val VERSION = 2
    }

    override fun toString() =
        "SinglesVersionV2[lastSyncs=$lastSyncs,planInternalId=$planInternalId,prefCityId=$prefCityId,...]"
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
        const val VERSION = 1
    }

    override fun toString() = "SinglesVersionV1[lastSync=$lastSync,planApiString=$planApiString,prefCityId=$prefCityId]"
}
