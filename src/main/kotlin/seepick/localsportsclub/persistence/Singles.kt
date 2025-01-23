package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object SinglesTable : Table("SINGLES") {
    val version = integer("VERSION")
    val json = text("JSON")
//    val notes = text("JSON")
//    val lastSync = datetime("LAST_SYNC").nullable()
//    val windowPref = varchar("WINDOW_PREF", 128).nullable()
//    val plan = varchar("USC_PLAN", 8).nullable()
//
//    // Preferences
//    val uscCredentialUsername = varchar("USC_CREDENTIAL_USERNAME", 64).nullable()
//    val uscCredentialPassword = varchar("USC_CREDENTIAL_PASSWORD", 128).nullable()
//    val uscCityId = integer("USC_CITY_ID").nullable()
//    val uscPeriodFirstDay = integer("USC_PERIOD_FIRST_DAY").nullable()
//    val googleCalendarId = varchar("GOOGLE_CALENDAR_ID", 64).nullable()
//    val homeLatitude = double("HOME_LATITUDE").nullable()
//    val homeLongitude = double("HOME_LONGITUDE").nullable()
}

data class SinglesDbo(
    val version: Int,
    val json: String,
//    val notes: String,
//    val lastSync: LocalDateTime?,
//    val windowPref: WindowPref?,
//    val plan: Plan?,
//    val preferences: Preferences,
) {
    override fun toString() =
        "SinglesDbo[version=$version, json=$json]"
}

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
                version = row[SinglesTable.version],
                json = row[SinglesTable.json],
//                notes = row[SinglesTable.notes],
//                lastSync = row[SinglesTable.lastSync],
//                plan = row[SinglesTable.plan]?.let { Plan.byApiString(it) },
//                windowPref = WindowPref.readFromSqlString(row[SinglesTable.windowPref]),
//                preferences = Preferences(
//                    uscCredentials = row[SinglesTable.uscCredentialUsername]?.let {
//                        Credentials(
//                            username = row[SinglesTable.uscCredentialUsername]!!,
//                            password = Encrypter.decrypt(row[SinglesTable.uscCredentialPassword]!!),
//                        )
//                    },
//                    periodFirstDay = row[SinglesTable.uscPeriodFirstDay],
//                    city = row[SinglesTable.uscCityId]?.let { City.byId(it) },
//                    gcal = row[SinglesTable.googleCalendarId]?.let {
//                        Gcal.GcalEnabled(it)
//                    } ?: Gcal.GcalDisabled,
//                    home = row[SinglesTable.homeLongitude]?.let {
//                        Location(
//                            longitude = row[SinglesTable.homeLongitude]!!,
//                            latitude = row[SinglesTable.homeLatitude]!!,
//                        )
//                    }
            )
        }
    }

    override fun insert(singles: SinglesDbo): Unit = transaction {
        log.debug { "insert($singles)" }
        require(SinglesTable.selectAll().toList().isEmpty())
        SinglesTable.insert {
            it[version] = singles.version
            it[json] = singles.json
//            it[windowPref] = singles.windowPref?.toSqlString()
//            it[plan] = singles.plan?.apiString
//            it[homeLatitude] = singles.preferences.home?.latitude
//            it[homeLongitude] = singles.preferences.home?.longitude
//            it[googleCalendarId] = singles.preferences.gcal.maybeCalendarId
//            it[uscCityId] = singles.preferences.city?.id
//            it[uscPeriodFirstDay] = singles.preferences.periodFirstDay
//            it[uscCredentialUsername] = singles.preferences.uscCredentials?.username
//            it[uscCredentialPassword] = singles.preferences.uscCredentials?.password?.let {
//                Encrypter.encrypt(it)
//            }
        }
    }

    override fun update(singles: SinglesDbo): Unit = transaction {
        log.debug { "update($singles)" }
        require(SinglesTable.selectAll().toList().size == 1)
        SinglesTable.update {
            it[version] = singles.version
            it[json] = singles.json
//            it[plan] = singles.plan?.apiString
//            it[windowPref] = singles.windowPref?.toSqlString()
//            it[homeLatitude] = singles.preferences.home?.latitude
//            it[homeLongitude] = singles.preferences.home?.longitude
//            it[googleCalendarId] = singles.preferences.gcal.maybeCalendarId
//            it[uscCityId] = singles.preferences.city?.id
//            it[uscPeriodFirstDay] = singles.preferences.periodFirstDay
//            it[uscCredentialUsername] = singles.preferences.uscCredentials?.username
//            it[uscCredentialPassword] = singles.preferences.uscCredentials?.password?.let {
//                Encrypter.encrypt(it)
//            }
        }
    }
}
