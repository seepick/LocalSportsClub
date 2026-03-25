package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object ActivityRemarkDboTable : IntIdTable("ACTIVITY_REMARKS", "ID") {
    val venueId = reference("VENUE_ID", VenueDboTable, fkName = "FK_ACTIVITY_REMARKS_VENUE_ID")
    val name = varchar("NAME", 128)
    val remark = text("REMARK")
    val rating = enumerationByName<RemarkDboRating>("RATING", 32)
}

data class ActivityRemarkDbo(
    val id: Int,
    val venueId: Int,
    val name: String,
    val remark: String,
    val rating: RemarkDboRating,
) {
    companion object {
        fun fromRow(row: ResultRow) = ActivityRemarkDbo(
            id = row[ActivityRemarkDboTable.id].value,
            venueId = row[ActivityRemarkDboTable.venueId].value,
            name = row[ActivityRemarkDboTable.name],
            remark = row[ActivityRemarkDboTable.remark],
            rating = row[ActivityRemarkDboTable.rating],
        )
    }
}

enum class RemarkDboRating {
    // CAVE: names are used for DB mapping!
    Amazing, Good, Meh, Bad
}

interface ActivityRemarkRepo {
    fun selectAll(): List<ActivityRemarkDbo>
    fun reset(venueId: Int, remarks: List<ActivityRemarkDbo>): List<ActivityRemarkDbo>
}

object ExposedActivityRemarkRepo : ActivityRemarkRepo {

    private val log = logger {}

    override fun selectAll() = transaction {
        ActivityRemarkDboTable.selectAll().map {
            ActivityRemarkDbo.fromRow(it)
        }
    }

    override fun reset(venueId: Int, remarks: List<ActivityRemarkDbo>) = transaction {
        log.debug { "reset(venueId=$venueId, remarks.size=${remarks.size})" }
        require(remarks.all { it.venueId == venueId }) {
            "All teachers must have the same venueId: $venueId ($remarks))"
        }

        ActivityRemarkDboTable.deleteWhere { this.venueId eq venueId }

        val nextId = ActivityRemarkDboTable.nextId()
        remarks.mapIndexed { index, oldDbo ->
            val newDbo = oldDbo.copy(id = nextId + index)
            ActivityRemarkDboTable.insert {
                prepareInsert(it, newDbo)
            }
            newDbo
        }
    }

    private fun prepareInsert(stmt: InsertStatement<Number>, dbo: ActivityRemarkDbo) {
        stmt[ActivityRemarkDboTable.id] = dbo.id
        stmt[ActivityRemarkDboTable.venueId] = dbo.venueId
        stmt[ActivityRemarkDboTable.name] = dbo.name
        stmt[ActivityRemarkDboTable.remark] = dbo.remark
        stmt[ActivityRemarkDboTable.rating] = dbo.rating
    }
}
