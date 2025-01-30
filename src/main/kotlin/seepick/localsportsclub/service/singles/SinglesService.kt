package seepick.localsportsclub.service.singles

import io.github.oshai.kotlinlogging.KotlinLogging.logger
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
    fun getLastSyncFor(city: City): LocalDateTime?
    fun setLastSyncFor(city: City, timestamp: LocalDateTime)
    var windowPref: WindowPref?
    var plan: Plan?
    var preferences: Preferences
    var verifiedUscCredentials: Credentials?
    var verifiedGcalId: String?
}

class SinglesServiceImpl(
    private val singlesRepo: SinglesRepo,
) : SinglesService {

    private val log = logger {}
    private var cache: SinglesVersionCurrent? = null

    override var notes
        get() = cachedOrSelect().notes
        set(value) {
            update { copy(notes = value) }
        }

    override fun getLastSyncFor(city: City): LocalDateTime? =
        cachedOrSelect().lastSyncs[city.id]

    override fun setLastSyncFor(city: City, timestamp: LocalDateTime) {
        update {
            copy(lastSyncs = lastSyncs.toMutableMap().also {
                it[city.id] = timestamp
            })
        }
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
    override var verifiedUscCredentials: Credentials?
        get() = cachedOrSelect().verifiedUscCredentials?.toCredentials()
        set(value) {
            update {
                copy(verifiedUscCredentials = value?.toJsonCredentials())
            }
        }
    override var verifiedGcalId: String?
        get() = cachedOrSelect().verifiedGcalId
        set(value) {
            update {
                copy(verifiedGcalId = value)
            }
        }

    override var preferences: Preferences
        get() {
            val single = cachedOrSelect()

            return Preferences(
                uscCredentials = single.prefUscCredentials?.toCredentials(),
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
                    prefUscCredentials = value.uscCredentials?.toJsonCredentials(),
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
        log.debug { "Loading stored: $cache" }
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

fun Credentials.toJsonCredentials() = JsonCredentials(
    username = username,
    encryptedPassword = Encrypter.encrypt(password),
)

fun JsonCredentials.toCredentials() = Credentials(
    username = username,
    password = Encrypter.decrypt(encryptedPassword),
)
