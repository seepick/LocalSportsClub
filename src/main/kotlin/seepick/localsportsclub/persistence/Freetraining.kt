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
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import seepick.localsportsclub.service.model.FreetrainingState
import java.time.LocalDate

data class FreetrainingDbo(
    val id: Int,
    val venueId: Int,
    val name: String,
    val category: String,
    val date: LocalDate,
    val state: FreetrainingState,
    val planId: Int,
) {
    val isScheduled = state == FreetrainingState.Scheduled
    val isCheckedin = state == FreetrainingState.Checkedin

    fun prepareInsert(statement: InsertStatement<Number>) {
        statement[FreetrainingDboTable.id] = this.id
        statement[FreetrainingDboTable.venueId] = this.venueId
        statement[FreetrainingDboTable.name] = this.name
        statement[FreetrainingDboTable.category] = this.category
        statement[FreetrainingDboTable.date] = this.date
        statement[FreetrainingDboTable.state] = this.state
        statement[FreetrainingDboTable.planId] = this.planId
    }

    fun prepareUpdate(statement: UpdateStatement) {
        statement[FreetrainingDboTable.name] = this.name
        statement[FreetrainingDboTable.category] = this.category
        statement[FreetrainingDboTable.date] = this.date
        statement[FreetrainingDboTable.state] = this.state
        statement[FreetrainingDboTable.planId] = this.planId
    }

    companion object {
        fun fromRow(row: ResultRow) = FreetrainingDbo(
            id = row[FreetrainingDboTable.id].value,
            venueId = row[FreetrainingDboTable.venueId].value,
            name = row[FreetrainingDboTable.name],
            category = row[FreetrainingDboTable.category],
            date = row[FreetrainingDboTable.date],
            state = row[FreetrainingDboTable.state],
            planId = row[FreetrainingDboTable.planId],
        )
    }
}

object FreetrainingDboTable : IntIdTable("FREETRAININGS", "ID") {
    val venueId = reference("VENUE_ID", VenueDboTable, fkName = "FK_FREETRAININGS_VENUE_ID")
    val name = varchar("NAME", 256)
    val category = varchar("CATEGORY", 64)
    val date = date("DATE")
    val state = enumerationByName<FreetrainingState>("STATE", 32)
    val planId = integer("PLAN_ID")
}

interface FreetrainingRepo {
    fun selectAll(cityId: Int): List<FreetrainingDbo>
    fun selectAllScheduled(cityId: Int): List<FreetrainingDbo>
    fun selectAllAnywhere(): List<FreetrainingDbo>
    fun selectFutureMostDate(cityId: Int): LocalDate?
    fun selectById(freetrainingId: Int): FreetrainingDbo?
    fun insert(dbo: FreetrainingDbo)
    fun update(dbo: FreetrainingDbo)
    fun deleteNonCheckedinBefore(threshold: LocalDate): List<FreetrainingDbo>
}

object ExposedFreetrainingRepo : FreetrainingRepo {

    private val log = logger {}

    override fun selectAll(cityId: Int): List<FreetrainingDbo> = transaction {
        FreetrainingDboTable
            .join(VenueDboTable, JoinType.LEFT, onColumn = FreetrainingDboTable.venueId, otherColumn = VenueDboTable.id)
            .selectAll().where { VenueDboTable.cityId eq cityId }.orderBy(FreetrainingDboTable.date).map {
                FreetrainingDbo.fromRow(it)
            }
    }

    override fun selectAllScheduled(cityId: Int): List<FreetrainingDbo> = transaction {
        FreetrainingDboTable
            .join(VenueDboTable, JoinType.LEFT, onColumn = FreetrainingDboTable.venueId, otherColumn = VenueDboTable.id)
            .selectAll()
            .where { (VenueDboTable.cityId eq cityId) and (FreetrainingDboTable.state eq FreetrainingState.Scheduled) }
            .map {
                FreetrainingDbo.fromRow(it)
            }
    }

    override fun selectAllAnywhere(): List<FreetrainingDbo> = transaction {
        FreetrainingDboTable.selectAll().orderBy(FreetrainingDboTable.date).map { FreetrainingDbo.fromRow(it) }
    }

    override fun selectFutureMostDate(cityId: Int): LocalDate? = transaction {
        FreetrainingDboTable
            .join(VenueDboTable, JoinType.LEFT, onColumn = FreetrainingDboTable.venueId, otherColumn = VenueDboTable.id)
            .select(FreetrainingDboTable.date)
            .where { VenueDboTable.cityId eq cityId }
            .orderBy(FreetrainingDboTable.date, SortOrder.DESC).limit(1)
            .toList().let {
                if (it.isEmpty()) null
                else it.first()[FreetrainingDboTable.date]
            }
    }

    override fun selectById(freetrainingId: Int): FreetrainingDbo? = transaction {
        FreetrainingDboTable.selectAll().where { FreetrainingDboTable.id.eq(freetrainingId) }.map {
            FreetrainingDbo.fromRow(it)
        }.singleOrNull()
    }

    override fun insert(dbo: FreetrainingDbo): Unit = transaction {
        log.debug { "Insert: $dbo" }
        FreetrainingDboTable.insert {
            dbo.prepareInsert(it)
        }
    }

    override fun update(dbo: FreetrainingDbo): Unit = transaction {
        val updated =
            FreetrainingDboTable.update(where = { FreetrainingDboTable.id.eq(dbo.id) }) { dbo.prepareUpdate(it) }
        if (updated != 1) error("Expected 1 to be updated by ID ${dbo.id}, but was: $updated")
    }

    override fun deleteNonCheckedinBefore(threshold: LocalDate): List<FreetrainingDbo> = transaction {
        val deleted = FreetrainingDboTable.selectAll().where {
            (FreetrainingDboTable.state neq FreetrainingState.Checkedin) and (FreetrainingDboTable.date less threshold)
        }.map { FreetrainingDbo.fromRow(it) }

        val deletedCount = FreetrainingDboTable.deleteWhere { FreetrainingDboTable.id.inList(deleted.map { it.id }) }
        require(deletedCount == deleted.size)

        log.info { "Deleted ${deleted.size} old freetrainings before $threshold." }
        deleted
    }
}
