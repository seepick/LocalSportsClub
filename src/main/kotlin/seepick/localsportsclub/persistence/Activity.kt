package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import seepick.localsportsclub.service.model.ActivityState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ActivityDbo(
    val id: Int,
    val venueId: Int,
    val name: String,
    val category: String, // aka disciplines/facilities
    val from: LocalDateTime,
    val to: LocalDateTime,
    // updateable
    val spotsLeft: Int,
    val teacher: String?,
    val state: ActivityState,
) {
    val isBooked = state == ActivityState.Booked
    val isCheckedin = state == ActivityState.Checkedin
    val nameWithTeacherIfPresent = if (teacher == null) name else "$name /$teacher"

    fun prepareInsert(statement: InsertStatement<Number>) {
        statement[ActivitiesTable.id] = this.id
        statement[ActivitiesTable.venueId] = this.venueId
        statement[ActivitiesTable.name] = this.name
        statement[ActivitiesTable.category] = this.category
        statement[ActivitiesTable.from] = this.from
        statement[ActivitiesTable.to] = this.to
        statement[ActivitiesTable.spotsLeft] = this.spotsLeft
        statement[ActivitiesTable.teacher] = this.teacher
        statement[ActivitiesTable.state] = this.state
    }

    fun prepareUpdate(update: UpdateStatement) {
        update[ActivitiesTable.teacher] = this.teacher
        update[ActivitiesTable.spotsLeft] = this.spotsLeft
        update[ActivitiesTable.state] = this.state
    }

    companion object {
        fun fromRow(row: ResultRow) = ActivityDbo(
            id = row[ActivitiesTable.id].value,
            venueId = row[ActivitiesTable.venueId].value,
            name = row[ActivitiesTable.name],
            category = row[ActivitiesTable.category],
            from = row[ActivitiesTable.from].withNano(0),
            to = row[ActivitiesTable.to].withNano(0),
            spotsLeft = row[ActivitiesTable.spotsLeft],
            teacher = row[ActivitiesTable.teacher],
            state = row[ActivitiesTable.state],
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
    val state = enumerationByName<ActivityState>("STATE", 32)
}

interface ActivityRepo {
    fun selectAll(): List<ActivityDbo>
    fun selectAllBooked(): List<ActivityDbo>
    fun insert(activity: ActivityDbo)
    fun update(activity: ActivityDbo)
    fun selectById(id: Int): ActivityDbo?
    fun selectFutureMostDate(): LocalDate?
    fun selectNewestCheckedinDate(): LocalDate?
    fun deleteBlanksBefore(threshold: LocalDate): List<ActivityDbo>
    fun selectAllForVenueId(venueId: Int): List<ActivityDbo>
}

class InMemoryActivityRepo : ActivityRepo {

    val stored = mutableMapOf<Int, ActivityDbo>()

    override fun selectAll() = stored.values.toList()

    override fun selectAllBooked() = stored.filter { it.value.state == ActivityState.Booked }.values.toList()

    override fun selectAllForVenueId(venueId: Int) = stored.values.filter { it.venueId == venueId }

    override fun selectById(id: Int): ActivityDbo? = stored[id]

    override fun selectFutureMostDate(): LocalDate? = stored.values.maxByOrNull { it.from }?.from?.toLocalDate()

    override fun selectNewestCheckedinDate(): LocalDate? =
        stored.values.filter { it.state == ActivityState.Checkedin }.maxByOrNull { it.from }?.from?.toLocalDate()

    override fun deleteBlanksBefore(threshold: LocalDate): List<ActivityDbo> {
        val deletingActivities = stored.values.filter {
            it.state == ActivityState.Blank && it.from.toLocalDate() < threshold
        }
        deletingActivities.forEach {
            stored.remove(it.id)
        }
        return deletingActivities
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
        ActivitiesTable.selectAll().orderBy(ActivitiesTable.from).map {
            ActivityDbo.fromRow(it)
        }
    }

    override fun selectById(id: Int) = transaction {
        ActivitiesTable.selectAll().where { ActivitiesTable.id.eq(id) }.map {
            ActivityDbo.fromRow(it)
        }.singleOrNull()
    }

    override fun selectAllBooked(): List<ActivityDbo> = transaction {
        ActivitiesTable.selectAll().orderBy(ActivitiesTable.from)
            .where { ActivitiesTable.state eq ActivityState.Booked }.map {
                ActivityDbo.fromRow(it)
            }
    }

    override fun selectAllForVenueId(venueId: Int): List<ActivityDbo> = transaction {
        ActivitiesTable.selectAll().orderBy(ActivitiesTable.from)
            .where { ActivitiesTable.venueId eq venueId }.map {
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
        ActivitiesTable.select(ActivitiesTable.from).where { ActivitiesTable.state.eq(ActivityState.Checkedin) }
            .orderBy(ActivitiesTable.from, SortOrder.DESC).limit(1).toList().let {
                if (it.isEmpty()) null
                else it.first()[ActivitiesTable.from].toLocalDate()
            }
    }

    override fun deleteBlanksBefore(threshold: LocalDate): List<ActivityDbo> = transaction {
        val thresholdDateTime = LocalDateTime.of(threshold, LocalTime.of(0, 0))

        val deletedActivities = ActivitiesTable.selectAll().where {
            (ActivitiesTable.state eq ActivityState.Blank) and (ActivitiesTable.from less thresholdDateTime)
        }.map { ActivityDbo.fromRow(it) }

        val deletedActivitiesCount =
            ActivitiesTable.deleteWhere { ActivitiesTable.id.inList(deletedActivities.map { it.id }) }
        require(deletedActivitiesCount == deletedActivities.size)

        log.info { "Deleted ${deletedActivities.size} old activities before $threshold." }
        deletedActivities
    }

    override fun insert(activity: ActivityDbo): Unit = transaction {
        log.debug { "Insert: $activity" }
        ActivitiesTable.insert {
            activity.prepareInsert(it)
        }
    }

    override fun update(activity: ActivityDbo): Unit = transaction {
        val updated = ActivitiesTable.update(where = { ActivitiesTable.id.eq(activity.id) }) {
            activity.prepareUpdate(it)
        }
        if (updated != 1) error("Expected 1 to be updated by ID ${activity.id}, but was: $updated")
    }

}
