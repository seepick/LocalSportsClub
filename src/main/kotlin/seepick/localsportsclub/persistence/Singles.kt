package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteAll
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
            )
        }
    }

    override fun insert(singles: SinglesDbo): Unit = transaction {
        log.debug { "insert($singles)" }
        require(SinglesTable.selectAll().toList().isEmpty())
        SinglesTable.insert {
            it[version] = singles.version
            it[json] = singles.json
        }
    }

    override fun update(singles: SinglesDbo): Unit = transaction {
        log.debug { "update($singles)" }
        require(SinglesTable.selectAll().toList().size == 1)
        SinglesTable.update {
            it[version] = singles.version
            it[json] = singles.json
        }
    }

    fun reset(): Unit = transaction {
        SinglesTable.deleteAll()
    }
}
