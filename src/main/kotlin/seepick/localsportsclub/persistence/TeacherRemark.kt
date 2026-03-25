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

object TeacherRemarkDboTable : IntIdTable("TEACHER_REMARKS", "ID") {
    val venueId = reference("VENUE_ID", VenueDboTable, fkName = "FK_TEACHER_REMARKS_VENUE_ID")
    val name = varchar("NAME", 128)
    val remark = text("REMARK")
    val rating = enumerationByName<RemarkDboRating>("RATING", 32)
}

data class TeacherRemarkDbo(
    val id: Int,
    val venueId: Int,
    val name: String,
    val remark: String,
    val rating: RemarkDboRating,
) {
    companion object {
        fun fromRow(row: ResultRow) = TeacherRemarkDbo(
            id = row[TeacherRemarkDboTable.id].value,
            venueId = row[TeacherRemarkDboTable.venueId].value,
            name = row[TeacherRemarkDboTable.name],
            remark = row[TeacherRemarkDboTable.remark],
            rating = row[TeacherRemarkDboTable.rating],
        )
    }
}

interface TeacherRemarkRepo {
    fun selectAll(): List<TeacherRemarkDbo>
    fun reset(venueId: Int, remarks: List<TeacherRemarkDbo>): List<TeacherRemarkDbo>
}

object ExposedTeacherRemarkRepo : TeacherRemarkRepo {

    private val log = logger {}

    override fun selectAll() = transaction {
        TeacherRemarkDboTable.selectAll().map {
            TeacherRemarkDbo.fromRow(it)
        }
    }

    override fun reset(venueId: Int, remarks: List<TeacherRemarkDbo>) = transaction {
        log.debug { "reset(venueId=$venueId, remarks.size=${remarks.size})" }
        require(remarks.all { it.venueId == venueId }) {
            "All teachers must have the same venueId: $venueId ($remarks))"
        }

        TeacherRemarkDboTable.deleteWhere { this.venueId eq venueId }

        val nextId = TeacherRemarkDboTable.nextId()
        remarks.mapIndexed { index, oldDbo ->
            val newDbo = oldDbo.copy(id = nextId + index)
            TeacherRemarkDboTable.insert {
                prepareInsert(it, newDbo)
            }
            newDbo
        }
    }

    private fun prepareInsert(stmt: InsertStatement<Number>, dbo: TeacherRemarkDbo) {
        stmt[TeacherRemarkDboTable.id] = dbo.id
        stmt[TeacherRemarkDboTable.venueId] = dbo.venueId
        stmt[TeacherRemarkDboTable.name] = dbo.name
        stmt[TeacherRemarkDboTable.rating] = dbo.rating
        stmt[TeacherRemarkDboTable.remark] = dbo.remark
    }
}
