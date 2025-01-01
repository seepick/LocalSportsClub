package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ActivityDbo(
    val id: Int,
    val venueId: Int,
    val name: String,
    val category: String, // aka disciplines/facilities
    val spotsLeft: Int,
    val from: LocalDateTime,
    val to: LocalDateTime, // TODO change to duratinInMins => easier to instantiate ;) in domain object, provide dateTimeRange still (with convenient ctor with duration)
    val teacher: String?,
    val isBooked: Boolean,
    val wasCheckedin: Boolean,
) {
    companion object {
        fun fromRow(row: ResultRow) = ActivityDbo(
            id = row[ActivitiesTable.id].value,
            venueId = row[ActivitiesTable.venueId].value,
            name = row[ActivitiesTable.name],
            category = row[ActivitiesTable.category],
            from = row[ActivitiesTable.from].withNano(0),
            to = row[ActivitiesTable.to].withNano(0),
            teacher = row[ActivitiesTable.teacher],
            spotsLeft = row[ActivitiesTable.spotsLeft],
            isBooked = row[ActivitiesTable.isBooked],
            wasCheckedin = row[ActivitiesTable.wasCheckedin],
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
    val teacher = varchar("TEACHER", 64).nullable()
    val isBooked = bool("IS_BOOKED")
    val wasCheckedin = bool("WAS_CHECKEDIN")
}

interface ActivityRepo {
    fun selectAll(): List<ActivityDbo>
    fun selectAllUpcoming(today: LocalDate): List<ActivityDbo>
    fun selectAllBooked(): List<ActivityDbo>
    fun insert(activity: ActivityDbo)
    fun update(activity: ActivityDbo)
    fun selectById(id: Int): ActivityDbo?
    fun selectFutureMostDate(): LocalDate?
    fun selectNewestCheckedinDate(): LocalDate?
    fun deleteNonBookedNonCheckedinBefore(threshold: LocalDate)
}

class InMemoryActivityRepo : ActivityRepo {

    val stored = mutableMapOf<Int, ActivityDbo>()

    override fun selectAll(): List<ActivityDbo> = stored.values.toList()
    override fun selectAllUpcoming(today: LocalDate): List<ActivityDbo> =
        stored.values.filter { it.from.toLocalDate() >= today }

    override fun selectAllBooked() = stored.filter { it.value.isBooked }.values.toList()

    override fun selectById(id: Int): ActivityDbo? = stored[id]

    override fun selectFutureMostDate(): LocalDate? = stored.values.maxByOrNull { it.from }?.from?.toLocalDate()

    override fun selectNewestCheckedinDate(): LocalDate? =
        stored.values.filter { it.wasCheckedin }.maxByOrNull { it.from }?.from?.toLocalDate()

    override fun deleteNonBookedNonCheckedinBefore(threshold: LocalDate) {
        stored.values.filter {
            !it.isBooked && !it.wasCheckedin && it.from.toLocalDate() < threshold
        }.forEach {
            stored.remove(it.id)
        }
    }

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

    override fun selectAllUpcoming(today: LocalDate): List<ActivityDbo> = transaction {
        val todayTime = LocalDateTime.of(today, LocalTime.of(0, 0))
        ActivitiesTable.selectAll().where { ActivitiesTable.from.greaterEq(todayTime) }.map {
            ActivityDbo.fromRow(it)
        }
    }

    override fun selectById(id: Int) = transaction {
        ActivitiesTable.selectAll().where { ActivitiesTable.id.eq(id) }.map {
            ActivityDbo.fromRow(it)
        }.singleOrNull()
    }

    override fun selectAllBooked(): List<ActivityDbo> = transaction {
        ActivitiesTable.selectAll().where { ActivitiesTable.isBooked.eq(true) }.map {
            ActivityDbo.fromRow(it)
        }
    }

    override fun selectFutureMostDate(): LocalDate? = transaction {
        ActivitiesTable.select(ActivitiesTable.from).orderBy(ActivitiesTable.from, SortOrder.DESC).limit(1).toList()
            .let {
                if (it.isEmpty()) null
                else it.first()[ActivitiesTable.from].toLocalDate()
            }
    }

    override fun selectNewestCheckedinDate(): LocalDate? = transaction {
        ActivitiesTable.select(ActivitiesTable.from).where { ActivitiesTable.wasCheckedin.eq(true) }
            .orderBy(ActivitiesTable.from, SortOrder.DESC).limit(1).toList().let {
                if (it.isEmpty()) null
                else it.first()[ActivitiesTable.from].toLocalDate()
            }
    }

    override fun deleteNonBookedNonCheckedinBefore(threshold: LocalDate): Unit = transaction {
        val thresholdDateTime = LocalDateTime.of(threshold, LocalTime.of(0, 0))
        val deleted = ActivitiesTable.deleteWhere {
            wasCheckedin.eq(false).and(isBooked.eq(false)).and(from.less(thresholdDateTime))
        }
        log.info { "Deleted $deleted old activities before $threshold." }
    }

    override fun insert(activity: ActivityDbo): Unit = transaction {
        log.debug { "Insert: $activity" }
        ActivitiesTable.insert {
            it[id] = activity.id
            it[venueId] = activity.venueId
            it[name] = activity.name
            it[category] = activity.category
            it[spotsLeft] = activity.spotsLeft
            it[teacher] = activity.teacher
            it[from] = activity.from
            it[to] = activity.to
            it[isBooked] = activity.isBooked
            it[wasCheckedin] = activity.wasCheckedin
        }
    }

    override fun update(activity: ActivityDbo): Unit = transaction {
        val updated = ActivitiesTable.update(where = { ActivitiesTable.id.eq(activity.id) }) {
            it[teacher] = teacher
            it[spotsLeft] = activity.spotsLeft
            it[isBooked] = activity.isBooked
            it[wasCheckedin] = wasCheckedin
        }
        if (updated != 1) error("Expected 1 to be updated by ID ${activity.id}, but was: $updated")
    }

}
