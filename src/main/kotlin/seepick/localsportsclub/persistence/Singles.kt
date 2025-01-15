package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.service.WindowPref
import java.time.LocalDateTime

object SinglesTable : Table("SINGLES") {
    val notes = text("NOTES")
    val lastSync = datetime("LAST_SYNC").nullable()
    val windowPref = varchar("WINDOW_PREF", 128).nullable()
    val homeLatitude = double("HOME_LATITUDE").nullable()
    val homeLongitude = double("HOME_LONGITUDE").nullable()
}

data class SinglesDbo(
    val notes: String,
    val lastSync: LocalDateTime?,
    val windowPref: WindowPref?,
    val home: Location?,
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
                home = row[SinglesTable.homeLatitude]?.let {
                    Location(longitude = row[SinglesTable.homeLongitude]!!, latitude = row[SinglesTable.homeLatitude]!!)
                }
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
            it[homeLatitude] = singles.home?.latitude
            it[homeLongitude] = singles.home?.longitude
        }
    }

    override fun update(singles: SinglesDbo): Unit = transaction {
        log.debug { "update($singles)" }
        require(SinglesTable.selectAll().toList().size == 1)
        SinglesTable.update {
            it[notes] = singles.notes
            it[lastSync] = singles.lastSync
            it[windowPref] = singles.windowPref?.toSqlString()
            it[homeLatitude] = singles.home?.latitude
            it[homeLongitude] = singles.home?.longitude
        }
    }
}
