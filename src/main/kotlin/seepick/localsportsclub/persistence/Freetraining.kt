package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.IntIdTable
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
import java.time.LocalDate

data class FreetrainingDbo(
    val id: Int,
    val venueId: Int,
    val name: String,
    val category: String,
    val date: LocalDate,
    val isScheduled: Boolean,
    val wasCheckedin: Boolean,
) {
    fun prepareInsert(statement: InsertStatement<Number>) {
        statement[FreetrainingsTable.id] = this.id
        statement[FreetrainingsTable.venueId] = this.venueId
        statement[FreetrainingsTable.name] = this.name
        statement[FreetrainingsTable.category] = this.category
        statement[FreetrainingsTable.date] = this.date
        statement[FreetrainingsTable.isScheduled] = this.isScheduled
        statement[FreetrainingsTable.wasCheckedin] = this.wasCheckedin
    }

    fun prepareUpdate(update: UpdateStatement) {
        update[FreetrainingsTable.name] = this.name
        update[FreetrainingsTable.category] = this.category
        update[FreetrainingsTable.date] = this.date
        update[FreetrainingsTable.isScheduled] = this.isScheduled
        update[FreetrainingsTable.wasCheckedin] = this.wasCheckedin
    }

    companion object {
        fun fromRow(row: ResultRow) = FreetrainingDbo(
            id = row[FreetrainingsTable.id].value,
            venueId = row[FreetrainingsTable.venueId].value,
            name = row[FreetrainingsTable.name],
            category = row[FreetrainingsTable.category],
            date = row[FreetrainingsTable.date],
            isScheduled = row[FreetrainingsTable.isScheduled],
            wasCheckedin = row[FreetrainingsTable.wasCheckedin],
        )
    }
}

object FreetrainingsTable : IntIdTable("PUBLIC.FREETRAININGS", "ID") {
    val venueId = reference("VENUE_ID", VenuesTable, fkName = "FK_FREETRAININGS_VENUE_ID")
    val name = varchar("NAME", 256)
    val category = varchar("CATEGORY", 64)
    val date = date("DATE")
    val isScheduled = bool("IS_SCHEDULED")
    val wasCheckedin = bool("WAS_CHECKEDIN")
}

interface FreetrainingRepo {
    fun selectAll(): List<FreetrainingDbo>
    fun selectById(freetrainingId: Int): FreetrainingDbo?
    fun selectAllScheduled(): List<FreetrainingDbo>
    fun selectFutureMostDate(): LocalDate?
    fun insert(dbo: FreetrainingDbo)
    fun update(dbo: FreetrainingDbo)
    fun deleteNonCheckedinBefore(threshold: LocalDate): List<FreetrainingDbo>
}

class InMemoryFreetrainingRepo : FreetrainingRepo {
    val stored = mutableMapOf<Int, FreetrainingDbo>()

    override fun selectAll() = stored.values.toList()

    override fun selectById(freetrainingId: Int): FreetrainingDbo? = stored[freetrainingId]
    override fun selectAllScheduled() = stored.values.filter { it.isScheduled }

    override fun selectFutureMostDate(): LocalDate? = stored.values.maxByOrNull { it.date }?.date

    override fun insert(dbo: FreetrainingDbo) {
        require(!stored.containsKey(dbo.id))
        stored[dbo.id] = dbo
    }

    override fun deleteNonCheckedinBefore(threshold: LocalDate): List<FreetrainingDbo> {
        val delete = stored.values.filter {
            !it.wasCheckedin && it.date < threshold
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

    override fun selectAll(): List<FreetrainingDbo> = transaction {
        FreetrainingsTable.selectAll().orderBy(FreetrainingsTable.date).map {
            FreetrainingDbo.fromRow(it)
        }
    }

    override fun selectById(freetrainingId: Int): FreetrainingDbo? = transaction {
        FreetrainingsTable.selectAll().where { FreetrainingsTable.id.eq(freetrainingId) }.map {
            FreetrainingDbo.fromRow(it)
        }.singleOrNull()
    }

    override fun selectAllScheduled(): List<FreetrainingDbo> = transaction {
        FreetrainingsTable.selectAll().where { FreetrainingsTable.isScheduled.eq(true) }.map {
            FreetrainingDbo.fromRow(it)
        }
    }

    override fun selectFutureMostDate(): LocalDate? = transaction {
        FreetrainingsTable.select(FreetrainingsTable.date).orderBy(FreetrainingsTable.date, SortOrder.DESC).limit(1)
            .toList().let {
                if (it.isEmpty()) null
                else it.first()[FreetrainingsTable.date]
            }
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
            FreetrainingsTable.wasCheckedin.eq(false).and(FreetrainingsTable.date.less(threshold))
        }.map { FreetrainingDbo.fromRow(it) }

        val deletedCount = FreetrainingsTable.deleteWhere { FreetrainingsTable.id.inList(deleted.map { it.id }) }
        require(deletedCount == deleted.size)

        log.info { "Deleted ${deleted.size} old freetrainings before $threshold." }
        deleted
    }
}
