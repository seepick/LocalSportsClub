package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.JoinType
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
    val cancellationLimit: LocalDateTime?,
    val planId: Int,
    val spotsLeft: Int, // updateable
    val teacher: String?, // updateable
    val state: ActivityState, // updateable
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
        statement[ActivitiesTable.state] = this.state
        statement[ActivitiesTable.teacher] = this.teacher
        statement[ActivitiesTable.spotsLeft] = this.spotsLeft
        statement[ActivitiesTable.cancellationLimit] = this.cancellationLimit
        statement[ActivitiesTable.planId] = this.planId
    }

    fun prepareUpdate(statement: UpdateStatement) {
        statement[ActivitiesTable.name] = this.name
        statement[ActivitiesTable.category] = this.category
        statement[ActivitiesTable.from] = this.from
        statement[ActivitiesTable.to] = this.to
        statement[ActivitiesTable.state] = this.state
        statement[ActivitiesTable.teacher] = this.teacher
        statement[ActivitiesTable.spotsLeft] = this.spotsLeft
        statement[ActivitiesTable.cancellationLimit] = this.cancellationLimit
        statement[ActivitiesTable.planId] = this.planId
    }

    companion object {
        fun fromRow(row: ResultRow) = ActivityDbo(
            id = row[ActivitiesTable.id].value,
            venueId = row[ActivitiesTable.venueId].value,
            name = row[ActivitiesTable.name],
            category = row[ActivitiesTable.category],
            from = row[ActivitiesTable.from].withNano(0),
            to = row[ActivitiesTable.to].withNano(0),
            state = row[ActivitiesTable.state],
            teacher = row[ActivitiesTable.teacher],
            spotsLeft = row[ActivitiesTable.spotsLeft],
            cancellationLimit = row[ActivitiesTable.cancellationLimit],
            planId = row[ActivitiesTable.planId],
        )
    }
}

object ActivitiesTable : IntIdTable("ACTIVITIES", "ID") {
    val name = varchar("NAME", 256) // sync list
    val category = varchar("CATEGORY", 64) // sync list
    val venueId = reference("VENUE_ID", VenuesTable, fkName = "FK_ACTIVITIES_VENUE_ID")
    val from = datetime("FROM_DATETIME")
    val to = datetime("TO_DATETIME")
    val spotsLeft = integer("SPOTS_LEFT")
    val teacher = varchar("TEACHER", 64).nullable()
    val state = enumerationByName<ActivityState>("STATE", 32)
    val cancellationLimit = datetime("CANCELLATION_LIMIT").nullable()
    val planId = integer("PLAN_ID")
}

interface ActivityRepo {
    fun selectAll(cityId: Int): List<ActivityDbo>
    fun selectAllBooked(cityId: Int): List<ActivityDbo>
    fun selectAllForVenueId(venueId: Int): List<ActivityDbo>
    fun selectById(id: Int): ActivityDbo?
    fun selectFutureMostDate(): LocalDate?
    fun selectNewestCheckedinDate(): LocalDate?
    fun insert(activity: ActivityDbo)
    fun update(activity: ActivityDbo)
    fun deleteBlanksBefore(threshold: LocalDate): List<ActivityDbo>
}

class InMemoryActivityRepo(
    private val venueRepo: VenueRepo? = null
) : ActivityRepo {

    val stored = mutableMapOf<Int, ActivityDbo>()

    override fun selectAll(cityId: Int): List<ActivityDbo> =
        if (venueRepo == null) stored.values.toList()
        else {
            val venueIdsInCity = venueRepo.selectAllByCity(cityId).map { it.id }.toSet()
            stored.values.filter { venueIdsInCity.contains(it.venueId) }
        }

    override fun selectAllBooked(cityId: Int): List<ActivityDbo> {
        val condition: (ActivityDbo) -> Boolean = if (venueRepo == null) {
            { _ -> true }
        } else {
            val venueIdsInCity = venueRepo.selectAllByCity(cityId).map { it.id }.toSet();
            { activity: ActivityDbo -> venueIdsInCity.contains(activity.venueId) }
        }
        return stored.values.filter { it.state == ActivityState.Booked && condition(it) }.toList()
    }

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

    override fun selectAll(cityId: Int): List<ActivityDbo> = transaction {
        ActivitiesTable
            .join(VenuesTable, JoinType.LEFT, onColumn = ActivitiesTable.venueId, otherColumn = VenuesTable.id)
            .selectAll().where { VenuesTable.cityId.eq(cityId) }.orderBy(ActivitiesTable.from).map {
                ActivityDbo.fromRow(it)
            }
    }

    override fun selectAllBooked(cityId: Int): List<ActivityDbo> = transaction {
        ActivitiesTable
            .join(VenuesTable, JoinType.LEFT, onColumn = ActivitiesTable.venueId, otherColumn = VenuesTable.id)
            .selectAll().where { (VenuesTable.cityId eq cityId) and (ActivitiesTable.state eq ActivityState.Booked) }
            .orderBy(ActivitiesTable.from).map {
                ActivityDbo.fromRow(it)
            }
    }

    override fun selectById(id: Int) = transaction {
        ActivitiesTable.selectAll().where { ActivitiesTable.id.eq(id) }.map {
            ActivityDbo.fromRow(it)
        }.singleOrNull()
    }

    override fun selectAllForVenueId(venueId: Int): List<ActivityDbo> = transaction {
        ActivitiesTable.selectAll().orderBy(ActivitiesTable.from).where { ActivitiesTable.venueId eq venueId }.map {
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
}
