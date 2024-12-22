package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

data class ActivityDbo(
    val id: Int,
    val venueId: Int,
    val name: String,
    val category: String, // aka disciplines/facilities
    val spotsLeft: Int,
    val from: LocalDateTime,
    val to: LocalDateTime,
) {
    companion object {
        fun fromRow(row: ResultRow) = ActivityDbo(
            id = row[ActivitiesTable.id].value,
            venueId = row[ActivitiesTable.venueId].value,
            name = row[ActivitiesTable.name],
            category = row[ActivitiesTable.category],
            from = row[ActivitiesTable.from],
            to = row[ActivitiesTable.to],
            spotsLeft = row[ActivitiesTable.spotsLeft],
        )
    }
}

object ActivitiesTable : IntIdTable("PUBLIC.ACTIVITIES", "ID") {
    val name = varchar("NAME", 256) // sync list
    val category = varchar("CATEGORY", 64) // sync list
    val venueId = reference("VENUE_ID", VenuesTable, fkName = "FK_ACTIVITIES_VENUE_ID")
    val from = datetime("FROM")
    val to = datetime("TO")
    val spotsLeft = integer("SPOTS_LEFT")
}

interface ActivityRepo {
    fun selectAll(): List<ActivityDbo>
    fun insert(activity: ActivityDbo)
    fun update(activity: ActivityDbo)
//     fun deleteAllBefore exceptWithCheckin (untilExclusive: LocalDate)
}

class InMemoryActivityRepo : ActivityRepo {
    val stored = mutableMapOf<Int, ActivityDbo>()
    override fun selectAll(): List<ActivityDbo> =
        stored.values.toList()

    override fun insert(activity: ActivityDbo) {
        require(!stored.containsKey(activity.id))
        stored[activity.id] = activity
    }

    override fun update(activity: ActivityDbo) {
        require(stored.containsKey(activity.id))
        stored[activity.id] = activity
    }

}

object ExposedActivityRepo : ActivityRepo {
    private val log = logger {}

    override fun selectAll(): List<ActivityDbo> = transaction {
        ActivitiesTable.selectAll().map {
            ActivityDbo.fromRow(it)
        }
    }

    override fun insert(activity: ActivityDbo): Unit = transaction {
        log.debug { "Insert: $activity" }
        ActivitiesTable.insert {
            it[id] = activity.id
            it[venueId] = activity.venueId
            it[name] = activity.name
            it[category] = activity.category
            it[spotsLeft] = activity.spotsLeft
            it[from] = activity.from
            it[to] = activity.to
        }
    }

    override fun update(activity: ActivityDbo): Unit = transaction {
        val updated = ActivitiesTable.update(where = { ActivitiesTable.id.eq(activity.id) }) {
            it[spotsLeft] = activity.spotsLeft
        }
        if (updated != 1) error("Expected 1 to be updated by ID ${activity.id}, but was: $updated")
    }

}
