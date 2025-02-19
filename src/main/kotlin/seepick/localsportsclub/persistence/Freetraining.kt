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
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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
        statement[FreetrainingsTable.id] = this.id
        statement[FreetrainingsTable.venueId] = this.venueId
        statement[FreetrainingsTable.name] = this.name
        statement[FreetrainingsTable.category] = this.category
        statement[FreetrainingsTable.date] = this.date
        statement[FreetrainingsTable.state] = this.state
        statement[FreetrainingsTable.planId] = this.planId
    }

    fun prepareUpdate(statement: UpdateStatement) {
        statement[FreetrainingsTable.name] = this.name
        statement[FreetrainingsTable.category] = this.category
        statement[FreetrainingsTable.date] = this.date
        statement[FreetrainingsTable.state] = this.state
        statement[FreetrainingsTable.planId] = this.planId
    }

    companion object {
        fun fromRow(row: ResultRow) = FreetrainingDbo(
            id = row[FreetrainingsTable.id].value,
            venueId = row[FreetrainingsTable.venueId].value,
            name = row[FreetrainingsTable.name],
            category = row[FreetrainingsTable.category],
            date = row[FreetrainingsTable.date],
            state = row[FreetrainingsTable.state],
            planId = row[FreetrainingsTable.planId],
        )
    }
}

object FreetrainingsTable : IntIdTable("FREETRAININGS", "ID") {
    val venueId = reference("VENUE_ID", VenuesTable, fkName = "FK_FREETRAININGS_VENUE_ID")
    val name = varchar("NAME", 256)
    val category = varchar("CATEGORY", 64)
    val date = date("DATE")
    val state = enumerationByName<FreetrainingState>("STATE", 32)
    val planId = integer("PLAN_ID")
}

interface FreetrainingRepo {
    fun selectAll(cityId: Int): List<FreetrainingDbo>
    fun selectAllScheduled(cityId: Int): List<FreetrainingDbo>
    fun selectFutureMostDate(cityId: Int): LocalDate?
    fun selectById(freetrainingId: Int): FreetrainingDbo?
    fun insert(dbo: FreetrainingDbo)
    fun update(dbo: FreetrainingDbo)
    fun deleteNonCheckedinBefore(threshold: LocalDate): List<FreetrainingDbo>
}

class InMemoryFreetrainingRepo : FreetrainingRepo {
    val stored = mutableMapOf<Int, FreetrainingDbo>()

    override fun selectAll(cityId: Int) = stored.values.toList()
    override fun selectAllScheduled(cityId: Int) = stored.values.filter { it.isScheduled }
    override fun selectFutureMostDate(cityId: Int): LocalDate? = stored.values.maxByOrNull { it.date }?.date
    override fun selectById(freetrainingId: Int): FreetrainingDbo? = stored[freetrainingId]

    override fun insert(dbo: FreetrainingDbo) {
        require(!stored.containsKey(dbo.id))
        stored[dbo.id] = dbo
    }

    override fun deleteNonCheckedinBefore(threshold: LocalDate): List<FreetrainingDbo> {
        val delete = stored.values.filter {
            it.state == FreetrainingState.Blank && it.date < threshold
        }
        delete.forEach {
            stored.remove(it.id)
        }
        return delete
    }

    override fun update(dbo: FreetrainingDbo) {
        require(stored.containsKey(dbo.id))
        stored[dbo.id] = dbo
    }
}

object ExposedFreetrainingRepo : FreetrainingRepo {

    private val log = logger {}

    override fun selectAll(cityId: Int): List<FreetrainingDbo> = transaction {
        FreetrainingsTable
            .join(VenuesTable, JoinType.LEFT, onColumn = FreetrainingsTable.venueId, otherColumn = VenuesTable.id)
            .selectAll().where { VenuesTable.cityId eq cityId }.orderBy(FreetrainingsTable.date).map {
                FreetrainingDbo.fromRow(it)
            }
    }

    override fun selectAllScheduled(cityId: Int): List<FreetrainingDbo> = transaction {
        FreetrainingsTable
            .join(VenuesTable, JoinType.LEFT, onColumn = FreetrainingsTable.venueId, otherColumn = VenuesTable.id)
            .selectAll()
            .where { (VenuesTable.cityId eq cityId) and (FreetrainingsTable.state eq FreetrainingState.Scheduled) }
            .map {
                FreetrainingDbo.fromRow(it)
            }
    }

    override fun selectFutureMostDate(cityId: Int): LocalDate? = transaction {
        FreetrainingsTable
            .join(VenuesTable, JoinType.LEFT, onColumn = FreetrainingsTable.venueId, otherColumn = VenuesTable.id)
            .select(FreetrainingsTable.date)
            .where { VenuesTable.cityId eq cityId }
            .orderBy(FreetrainingsTable.date, SortOrder.DESC).limit(1)
            .toList().let {
                if (it.isEmpty()) null
                else it.first()[FreetrainingsTable.date]
            }
    }

    override fun selectById(freetrainingId: Int): FreetrainingDbo? = transaction {
        FreetrainingsTable.selectAll().where { FreetrainingsTable.id.eq(freetrainingId) }.map {
            FreetrainingDbo.fromRow(it)
        }.singleOrNull()
    }

    override fun insert(dbo: FreetrainingDbo): Unit = transaction {
        log.debug { "Insert: $dbo" }
        FreetrainingsTable.insert {
            dbo.prepareInsert(it)
        }
    }

    override fun update(dbo: FreetrainingDbo): Unit = transaction {
        val updated = FreetrainingsTable.update(where = { FreetrainingsTable.id.eq(dbo.id) }) { dbo.prepareUpdate(it) }
        if (updated != 1) error("Expected 1 to be updated by ID ${dbo.id}, but was: $updated")
    }

    override fun deleteNonCheckedinBefore(threshold: LocalDate): List<FreetrainingDbo> = transaction {
        val deleted = FreetrainingsTable.selectAll().where {
            (FreetrainingsTable.state neq FreetrainingState.Checkedin) and (FreetrainingsTable.date less threshold)
        }.map { FreetrainingDbo.fromRow(it) }

        val deletedCount = FreetrainingsTable.deleteWhere { FreetrainingsTable.id.inList(deleted.map { it.id }) }
        require(deletedCount == deleted.size)

        log.info { "Deleted ${deleted.size} old freetrainings before $threshold." }
        deleted
    }
}
