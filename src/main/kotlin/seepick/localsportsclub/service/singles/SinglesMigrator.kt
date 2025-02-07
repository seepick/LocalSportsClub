package seepick.localsportsclub.service.singles

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.serialization.json.Json
import seepick.localsportsclub.persistence.SinglesDbo
import seepick.localsportsclub.service.model.City

object SinglesMigrator {

    private val log = logger {}

    fun migrate(dbo: SinglesDbo): SinglesVersionCurrent {
        require(dbo.version != SinglesVersionCurrent.VERSION)
        log.debug { "Migrating singles from version ${dbo.version} to ${SinglesVersionCurrent.VERSION}" }

        val v2 = if (dbo.version == 1) {
            log.info { "Migrating singles form v1 to v2" }
            migrateV1toV2(Json.decodeFromString<SinglesVersionV1>(dbo.json))
        } else null

        if (v2 != null || dbo.version == 2) {
            log.info { "Migrating singles form v2 to v3/current" }
            return migrateV2toV3(v2 ?: Json.decodeFromString<SinglesVersionV2>(dbo.json))
        }
        error("Unhandled version: ${dbo.version}")
    }

    private fun migrateV2toV3(old: SinglesVersionV2) = SinglesVersionCurrent(
        verifiedUscCredentials = null, // init with null
        verifiedGcalId = null, // init with null
        prefUscCredentials = old.prefUscCredUsername?.let { // combine
            JsonCredentials(old.prefUscCredUsername, old.prefUscCredPassword!!)
        },

        planInternalId = old.planInternalId,
        notes = old.notes,
        lastSyncs = old.lastSyncs,
        windowWidth = old.windowWidth,
        windowHeight = old.windowHeight,
        windowPosX = old.windowPosX,
        windowPosY = old.windowPosY,
        prefCityId = old.prefCityId,
        prefGoogleCalendarId = old.prefGoogleCalendarId,
        prefHomeLat = old.prefHomeLat,
        prefHomeLong = old.prefHomeLong,
        prefPeriodFirstDay = old.prefPeriodFirstDay,
    )

    private fun migrateV1toV2(old: SinglesVersionV1) = SinglesVersionV2(
        planInternalId = null, // needs to be refetched
        lastSyncs = buildMap {
            // supporting last-sync timestamp by configured city
            if (old.lastSync != null) {
                put(City.Amsterdam.id, old.lastSync)
            }
        },

        notes = old.notes,
        windowWidth = old.windowWidth,
        windowHeight = old.windowHeight,
        windowPosX = old.windowPosX,
        windowPosY = old.windowPosY,
        prefUscCredUsername = old.prefUscCredUsername,
        prefUscCredPassword = old.prefUscCredPassword,
        prefCityId = old.prefCityId,
        prefGoogleCalendarId = old.prefGoogleCalendarId,
        prefHomeLat = old.prefHomeLat,
        prefHomeLong = old.prefHomeLong,
        prefPeriodFirstDay = old.prefPeriodFirstDay,
    )
}
