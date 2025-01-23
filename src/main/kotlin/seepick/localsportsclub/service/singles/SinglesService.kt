package seepick.localsportsclub.service.singles

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import seepick.localsportsclub.persistence.SinglesDbo
import seepick.localsportsclub.persistence.SinglesRepo
import seepick.localsportsclub.service.Encrypter
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.service.WindowPref
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.Credentials
import seepick.localsportsclub.service.model.Gcal
import seepick.localsportsclub.service.model.Plan
import seepick.localsportsclub.service.model.Preferences
import java.time.LocalDateTime

interface SinglesService {
    var notes: String?
    var lastSync: LocalDateTime?
    var windowPref: WindowPref?
    var plan: Plan?
    var preferences: Preferences
}

class SinglesServiceImpl(
    private val singlesRepo: SinglesRepo,
) : SinglesService {

    private var cache: SinglesVersionCurrent? = null

    override var notes
        get() = cachedOrSelect().notes
        set(value) {
            update { copy(notes = value) }
        }
    override var lastSync: LocalDateTime?
        get() = cachedOrSelect().lastSync
        set(value) {
            update { copy(lastSync = value) }
        }

    override var windowPref: WindowPref?
        get() {
            val single = cachedOrSelect()
            return if (single.windowWidth == null) null else WindowPref(
                width = single.windowWidth,
                height = single.windowHeight!!,
                posX = single.windowPosX!!,
                posY = single.windowPosY!!,
            )
        }
        set(value) {
            update {
                if (value == null) copy(windowWidth = null, windowHeight = null, windowPosX = null, windowPosY = null)
                else copy(
                    windowWidth = value.width,
                    windowHeight = value.height,
                    windowPosX = value.posX,
                    windowPosY = value.posY,
                )
            }
        }

    override var plan: Plan?
        get() = cachedOrSelect().planInternalId?.let { Plan.byInternalId(it) }
        set(value) {
            update {
                if (value == null) copy(planInternalId = null)
                else copy(planInternalId = value.internalId)
            }
        }

    override var preferences: Preferences
        get() {
            val single = cachedOrSelect()

            return Preferences(
                uscCredentials = single.prefUscCredUsername?.let {
                    Credentials(
                        username = single.prefUscCredUsername,
                        password = single.prefUscCredPassword!!.let { Encrypter.decrypt(it) }
                    )
                },
                city = single.prefCityId?.let { City.byId(it) },
                gcal = single.prefGoogleCalendarId?.let { Gcal.GcalEnabled(it) } ?: Gcal.GcalDisabled,
                home = single.prefHomeLat?.let {
                    Location(
                        latitude = single.prefHomeLat,
                        longitude = single.prefHomeLong!!
                    )
                },
                periodFirstDay = single.prefPeriodFirstDay,
            )
        }
        set(value) {
            update {
                copy(
                    prefUscCredUsername = value.uscCredentials?.username,
                    prefUscCredPassword = value.uscCredentials?.password?.let { Encrypter.encrypt(it) },
                    prefCityId = value.city?.id,
                    prefGoogleCalendarId = value.gcal.maybeCalendarId,
                    prefHomeLat = value.home?.latitude,
                    prefHomeLong = value.home?.longitude,
                    prefPeriodFirstDay = value.periodFirstDay,
                )
            }
        }

    private fun update(withDbo: SinglesVersionCurrent.() -> SinglesVersionCurrent) {
        val dbo = cachedOrSelect()
        val updated = dbo.withDbo()
        singlesRepo.updateSingles(updated)
        cache = updated
    }

    private fun cachedOrSelect(): SinglesVersionCurrent {
        if (cache != null) return cache!!

        val stored = singlesRepo.select()
        if (stored == null) {
            cache = SinglesVersionCurrent.empty
            singlesRepo.insert(
                SinglesDbo(
                    version = SinglesVersionCurrent.VERSION,
                    json = Json.encodeToString(cache!!)
                )
            )
            return cache!!
        }

        cache = if (stored.version == SinglesVersionCurrent.VERSION) {
            Json.decodeFromString(stored.json)
        } else {
            val migrated = SinglesMigrator.migrate(stored)
            singlesRepo.updateSingles(migrated)
            migrated
        }
        return cache!!
    }

    private fun SinglesRepo.updateSingles(singles: SinglesVersionCurrent) {
        update(
            SinglesDbo(
                version = SinglesVersionCurrent.VERSION,
                json = Json.encodeToString(singles)
            )
        )
    }
}
