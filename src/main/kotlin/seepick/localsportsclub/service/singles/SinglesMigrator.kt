package seepick.localsportsclub.service.singles

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.serialization.json.Json
import seepick.localsportsclub.persistence.SinglesDbo

object SinglesMigrator {

    private val log = logger {}
    fun migrate(dbo: SinglesDbo): SinglesVersionCurrent {
        require(dbo.version != SinglesVersionCurrent.VERSION)
        log.debug { "Migrating singles from version ${dbo.version} to ${SinglesVersionCurrent.VERSION}" }
        if (dbo.version == 1) {
            return migrateV1(Json.decodeFromString<SinglesVersionV1>(dbo.json))
        }
        error("Unhandled version: ${dbo.version}")
    }

    private fun migrateV1(v1: SinglesVersionV1) = SinglesVersionCurrent(
        planInternalId = null, // needs to be refetched

        notes = v1.notes,
        lastSync = v1.lastSync,
        windowWidth = v1.windowWidth,
        windowHeight = v1.windowHeight,
        windowPosX = v1.windowPosX,
        windowPosY = v1.windowPosY,
        prefUscCredUsername = v1.prefUscCredUsername,
        prefUscCredPassword = v1.prefUscCredPassword,
        prefCityId = v1.prefCityId,
        prefGoogleCalendarId = v1.prefGoogleCalendarId,
        prefHomeLat = v1.prefHomeLat,
        prefHomeLong = v1.prefHomeLong,
        prefPeriodFirstDay = v1.prefPeriodFirstDay,
    )
}
