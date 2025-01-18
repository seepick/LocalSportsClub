package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import seepick.localsportsclub.api.Credentials
import seepick.localsportsclub.api.Encrypter
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.service.WindowPref
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.Gcal
import seepick.localsportsclub.service.model.Preferences
import java.time.LocalDateTime

object SinglesTable : Table("SINGLES") {
    val notes = text("NOTES")
    val lastSync = datetime("LAST_SYNC").nullable()
    val windowPref = varchar("WINDOW_PREF", 128).nullable()

    // Preferences
    val homeLatitude = double("HOME_LATITUDE").nullable()
    val homeLongitude = double("HOME_LONGITUDE").nullable()
    val googleCalendarId = varchar("GOOGLE_CALENDAR_ID", 64).nullable()
    val uscCityId = integer("USC_CITY_ID").nullable()
    val uscCredentialUsername = varchar("USC_CREDENTIAL_USERNAME", 64).nullable()
    val uscCredentialPassword = varchar("USC_CREDENTIAL_PASSWORD", 128).nullable()
}

data class SinglesDbo(
    val notes: String,
    val lastSync: LocalDateTime?,
    val windowPref: WindowPref?,
    val preferences: Preferences,
)

interface SinglesRepo {
    fun select(): SinglesDbo?
    fun insert(singles: SinglesDbo)
    fun update(singles: SinglesDbo)
}

class InMemorySinglesRepo : SinglesRepo {
    var stored: SinglesDbo? = null
    override fun select(): SinglesDbo? = stored

    override fun insert(singles: SinglesDbo) {
        require(stored == null)
        stored = singles
    }

    override fun update(singles: SinglesDbo) {
        require(stored != null)
        stored = singles
    }
}

object ExposedSinglesRepo : SinglesRepo {

    private val log = logger {}

    override fun select() = transaction {
        log.debug { "select()" }
        SinglesTable.selectAll().singleOrNull()?.let { row ->
            SinglesDbo(
                notes = row[SinglesTable.notes],
                lastSync = row[SinglesTable.lastSync],
                windowPref = WindowPref.readFromSqlString(row[SinglesTable.windowPref]),
                preferences = Preferences(
                    uscCredentials = row[SinglesTable.uscCredentialUsername]?.let {
                        Credentials(
                            username = row[SinglesTable.uscCredentialUsername]!!,
                            password = Encrypter.decrypt(row[SinglesTable.uscCredentialPassword]!!),
                        )
                    },
                    city = row[SinglesTable.uscCityId]?.let { City.byId(it) },
                    gcal = row[SinglesTable.googleCalendarId]?.let {
                        Gcal.GcalEnabled(it)
                    } ?: Gcal.GcalDisabled,
                    home = row[SinglesTable.homeLongitude]?.let {
                        Location(
                            longitude = row[SinglesTable.homeLongitude]!!,
                            latitude = row[SinglesTable.homeLatitude]!!,
                        )
                    }
                )
            )
        }
    }

    override fun insert(singles: SinglesDbo): Unit = transaction {
        log.debug { "insert($singles)" }
        require(SinglesTable.selectAll().toList().isEmpty())
        SinglesTable.insert {
            it[notes] = singles.notes
            it[lastSync] = singles.lastSync
            it[windowPref] = singles.windowPref?.toSqlString()
            it[homeLatitude] = singles.preferences.home?.latitude
            it[homeLongitude] = singles.preferences.home?.longitude
            it[googleCalendarId] = singles.preferences.gcal.maybeCalendarId
            it[uscCityId] = singles.preferences.city?.id
            it[uscCredentialUsername] = singles.preferences.uscCredentials?.username
            it[uscCredentialPassword] = singles.preferences.uscCredentials?.password?.let {
                Encrypter.encrypt(it)
            }
        }
    }

    override fun update(singles: SinglesDbo): Unit = transaction {
        log.debug { "update($singles)" }
        require(SinglesTable.selectAll().toList().size == 1)
        SinglesTable.update {
            it[notes] = singles.notes
            it[lastSync] = singles.lastSync
            it[windowPref] = singles.windowPref?.toSqlString()
            it[homeLatitude] = singles.preferences.home?.latitude
            it[homeLongitude] = singles.preferences.home?.longitude
            it[googleCalendarId] = singles.preferences.gcal.maybeCalendarId
            it[uscCityId] = singles.preferences.city?.id
            it[uscCredentialUsername] = singles.preferences.uscCredentials?.username
            it[uscCredentialPassword] = singles.preferences.uscCredentials?.password?.let {
                Encrypter.encrypt(it)
            }
        }
    }
}
