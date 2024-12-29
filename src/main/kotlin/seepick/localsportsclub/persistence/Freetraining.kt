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
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.time
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalTime

data class FreetrainingDbo(
    val id: Int,
    val venueId: Int,
    val name: String,
    val category: String,
    val date: LocalDate,
    val checkedinTime: LocalTime?,
) {
    fun prepareInsert(statement: InsertStatement<Number>) {
        statement[FreetrainingsTable.id] = this.id
        statement[FreetrainingsTable.venueId] = this.venueId
        statement[FreetrainingsTable.name] = this.name
        statement[FreetrainingsTable.category] = this.category
        statement[FreetrainingsTable.date] = this.date
        statement[FreetrainingsTable.checkedinTime] = this.checkedinTime
    }

    companion object {
        fun fromRow(row: ResultRow) = FreetrainingDbo(
            id = row[FreetrainingsTable.id].value,
            venueId = row[FreetrainingsTable.venueId].value,
            name = row[FreetrainingsTable.name],
            category = row[FreetrainingsTable.category],
            date = row[FreetrainingsTable.date],
            checkedinTime = row[FreetrainingsTable.checkedinTime]?.withNano(0),
        )
    }
}

object FreetrainingsTable : IntIdTable("PUBLIC.FREETRAININGS", "ID") {
    val venueId = reference("VENUE_ID", VenuesTable, fkName = "FK_FREETRAININGS_VENUE_ID")
    val name = varchar("NAME", 256)
    val category = varchar("CATEGORY", 64)
    val date = date("DATE")
    val checkedinTime = time("CHECKEDIN_TIME").nullable()
}

interface FreetrainingRepo {
    fun selectAll(): List<FreetrainingDbo>
    fun selectFutureMostDate(): LocalDate?
    fun insert(dbo: FreetrainingDbo)
    fun deleteNonBookedNonCheckedinBefore(threshold: LocalDate)
}

class InMemoryFreetrainingRepo : FreetrainingRepo {
    val stored = mutableListOf<FreetrainingDbo>()
    override fun selectAll() = stored

    override fun selectFutureMostDate(): LocalDate? =
        stored.maxByOrNull { it.date }?.date

    override fun insert(dbo: FreetrainingDbo) {
        stored.add(dbo)
    }

    override fun deleteNonBookedNonCheckedinBefore(threshold: LocalDate) {
        stored.filter {
            it.checkedinTime == null && it.date < threshold
        }.forEach {
            stored.remove(it)
        }
    }

}

object ExposedFreetrainingRepo : FreetrainingRepo {

    private val log = logger {}

    override fun selectAll(): List<FreetrainingDbo> = transaction {
        FreetrainingsTable.selectAll().map {
            FreetrainingDbo.fromRow(it)
        }
    }

    override fun selectFutureMostDate(): LocalDate? = transaction {
        FreetrainingsTable.select(FreetrainingsTable.date).orderBy(FreetrainingsTable.date, SortOrder.DESC).limit(1)
            .toList()
            .let {
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

    override fun deleteNonBookedNonCheckedinBefore(threshold: LocalDate) = transaction {
        val deleted = FreetrainingsTable.deleteWhere {
            checkedinTime.eq(null).and(date.less(threshold))
        }
        log.info { "Deleted $deleted old freetrainings before $threshold." }
    }

    // selectById
    // update
}
