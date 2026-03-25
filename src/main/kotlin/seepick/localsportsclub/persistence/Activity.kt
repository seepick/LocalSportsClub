package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
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
    val planId: Int, // plans for activities can change within the same venue (see e.g. "Het Gymlokaal Noord")

    val spotsLeft: Int, // updateable
    val state: ActivityState, // updateable
    val teacher: String?, // updateable
    val description: String?, // updateable
) {
    val isBooked = state == ActivityState.Booked
    val isCheckedin = state == ActivityState.Checkedin

    fun prepareInsert(statement: InsertStatement<Number>) {
        statement[ActivityDboTable.id] = this.id
        statement[ActivityDboTable.venueId] = this.venueId
        statement[ActivityDboTable.name] = this.name
        statement[ActivityDboTable.category] = this.category
        statement[ActivityDboTable.from] = this.from
        statement[ActivityDboTable.to] = this.to
        statement[ActivityDboTable.state] = this.state
        statement[ActivityDboTable.teacher] = this.teacher
        statement[ActivityDboTable.description] = this.description
        statement[ActivityDboTable.spotsLeft] = this.spotsLeft
        statement[ActivityDboTable.cancellationLimit] = this.cancellationLimit
        statement[ActivityDboTable.planId] = this.planId
    }

    fun prepareUpdate(statement: UpdateStatement) {
        statement[ActivityDboTable.name] = this.name
        statement[ActivityDboTable.category] = this.category
        statement[ActivityDboTable.from] = this.from
        statement[ActivityDboTable.to] = this.to
        statement[ActivityDboTable.state] = this.state
        statement[ActivityDboTable.teacher] = this.teacher
        statement[ActivityDboTable.description] = this.description
        statement[ActivityDboTable.spotsLeft] = this.spotsLeft
        statement[ActivityDboTable.cancellationLimit] = this.cancellationLimit
        statement[ActivityDboTable.planId] = this.planId
    }

    companion object {
        fun fromRow(row: ResultRow) = ActivityDbo(
            id = row[ActivityDboTable.id].value,
            venueId = row[ActivityDboTable.venueId].value,
            name = row[ActivityDboTable.name],
            category = row[ActivityDboTable.category],
            from = row[ActivityDboTable.from].withNano(0),
            to = row[ActivityDboTable.to].withNano(0),
            state = row[ActivityDboTable.state],
            teacher = row[ActivityDboTable.teacher],
            description = row[ActivityDboTable.description],
            spotsLeft = row[ActivityDboTable.spotsLeft],
            cancellationLimit = row[ActivityDboTable.cancellationLimit],
            planId = row[ActivityDboTable.planId],
        )
    }
}

object ActivityDboTable : IntIdTable("ACTIVITIES", "ID") {
    val name = varchar("NAME", 256) // sync list
    val category = varchar("CATEGORY", 64) // sync list
    val venueId = reference(name = "VENUE_ID", foreign = VenueDboTable, fkName = "FK_ACTIVITIES_VENUE_ID")
    val from = datetime("FROM_DATETIME")
    val to = datetime("TO_DATETIME")
    val spotsLeft = integer("SPOTS_LEFT")
    val teacher = varchar("TEACHER", 64).nullable()
    val description = text("DESCRIPTION").nullable()
    val state = enumerationByName<ActivityState>("STATE", 32)
    val cancellationLimit = datetime("CANCELLATION_LIMIT").nullable()
    val planId = integer("PLAN_ID")
}

interface ActivityRepo {
    fun selectAll(cityId: Int): List<ActivityDbo>
    fun selectAllBooked(cityId: Int): List<ActivityDbo>
    fun selectAllAnywhere(): List<ActivityDbo>
    fun selectAllForVenueId(venueId: Int): List<ActivityDbo>
    fun selectById(id: Int): ActivityDbo?
    fun selectFutureMostDate(): LocalDate?
    fun selectNewestCheckedinDate(): LocalDate?
    fun insert(activity: ActivityDbo)
    fun update(activity: ActivityDbo)
    fun deleteBlanksBefore(threshold: LocalDate): List<ActivityDbo>
}

object ExposedActivityRepo : ActivityRepo {
    private val log = logger {}

    override fun selectAll(cityId: Int): List<ActivityDbo> = transaction {
        ActivityDboTable
            .join(VenueDboTable, JoinType.LEFT, onColumn = ActivityDboTable.venueId, otherColumn = VenueDboTable.id)
            .selectAll().where { VenueDboTable.cityId.eq(cityId) }.orderBy(ActivityDboTable.from).map {
                ActivityDbo.fromRow(it)
            }
    }

    override fun selectAllBooked(cityId: Int): List<ActivityDbo> = transaction {
        ActivityDboTable
            .join(VenueDboTable, JoinType.LEFT, onColumn = ActivityDboTable.venueId, otherColumn = VenueDboTable.id)
            .selectAll().where { (VenueDboTable.cityId eq cityId) and (ActivityDboTable.state eq ActivityState.Booked) }
            .orderBy(ActivityDboTable.from).map {
                ActivityDbo.fromRow(it)
            }
    }

    override fun selectAllAnywhere(): List<ActivityDbo> = transaction {
        ActivityDboTable.selectAll().orderBy(ActivityDboTable.from).map {
            ActivityDbo.fromRow(it)
        }
    }

    override fun selectById(id: Int) = transaction {
        ActivityDboTable.selectAll().where { ActivityDboTable.id.eq(id) }.map {
            ActivityDbo.fromRow(it)
        }.singleOrNull()
    }

    override fun selectAllForVenueId(venueId: Int): List<ActivityDbo> = transaction {
        ActivityDboTable.selectAll().orderBy(ActivityDboTable.from).where { ActivityDboTable.venueId eq venueId }.map {
            ActivityDbo.fromRow(it)
        }
    }

    override fun selectFutureMostDate(): LocalDate? = transaction {
        ActivityDboTable.select(ActivityDboTable.from).orderBy(ActivityDboTable.from, SortOrder.DESC).limit(1).toList()
            .let {
                if (it.isEmpty()) null
                else it.first()[ActivityDboTable.from].toLocalDate()
            }
    }

    override fun selectNewestCheckedinDate(): LocalDate? = transaction {
        ActivityDboTable.select(ActivityDboTable.from).where { ActivityDboTable.state.eq(ActivityState.Checkedin) }
            .orderBy(ActivityDboTable.from, SortOrder.DESC).limit(1).toList().let {
                if (it.isEmpty()) null
                else it.first()[ActivityDboTable.from].toLocalDate()
            }
    }

    override fun insert(activity: ActivityDbo): Unit = transaction {
        log.debug { "Insert: $activity" }
        ActivityDboTable.insert {
            activity.prepareInsert(it)
        }
    }

    override fun update(activity: ActivityDbo): Unit = transaction {
        val updated = ActivityDboTable.update(where = { ActivityDboTable.id.eq(activity.id) }) {
            activity.prepareUpdate(it)
        }
        if (updated != 1) error("Expected 1 to be updated by ID ${activity.id}, but was: $updated")
    }

    override fun deleteBlanksBefore(threshold: LocalDate): List<ActivityDbo> = transaction {
        val thresholdDateTime = LocalDateTime.of(threshold, LocalTime.of(0, 0))

        val deletedActivities = ActivityDboTable.selectAll().where {
            (ActivityDboTable.state eq ActivityState.Blank) and (ActivityDboTable.from less thresholdDateTime)
        }.map { ActivityDbo.fromRow(it) }

        val deletedActivitiesCount =
            ActivityDboTable.deleteWhere { ActivityDboTable.id.inList(deletedActivities.map { it.id }) }
        require(deletedActivitiesCount == deletedActivities.size)

        log.info { "Deleted ${deletedActivities.size} old activities before $threshold." }
        deletedActivities
    }
}
