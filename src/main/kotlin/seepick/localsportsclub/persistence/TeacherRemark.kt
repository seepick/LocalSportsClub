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

object TeacherRemarksTable : IntIdTable("TEACHER_REMARKS", "ID") {
    val venueId = reference("VENUE_ID", VenuesTable, fkName = "FK_TEACHER_REMARKS_VENUE_ID")
    val name = varchar("NAME", 128)
    val remark = text("REMARK")
    val rating = enumerationByName<TeacherRemarkDboRating>("RATING", 32)
}

data class TeacherRemarkDbo(
    val id: Int,
    val venueId: Int,
    val name: String,
    val remark: String,
    val rating: TeacherRemarkDboRating,
) {
    companion object {
        fun fromRow(row: ResultRow) = TeacherRemarkDbo(
            id = row[TeacherRemarksTable.id].value,
            venueId = row[TeacherRemarksTable.venueId].value,
            name = row[TeacherRemarksTable.name],
            remark = row[TeacherRemarksTable.remark],
            rating = row[TeacherRemarksTable.rating],
        )
    }
}

enum class TeacherRemarkDboRating {
    // CAVE: names are used for DB mapping!
    Amazing, Good, Meh, Bad
}

interface TeacherRemarkRepo {
    fun selectAll(): List<TeacherRemarkDbo>
    fun reset(venueId: Int, remarks: List<TeacherRemarkDbo>): List<TeacherRemarkDbo>
}

object ExposedTeacherRemarkRepo : TeacherRemarkRepo {

    private val log = logger {}

    override fun selectAll() = transaction {
        TeacherRemarksTable.selectAll().map {
            TeacherRemarkDbo.fromRow(it)
        }
    }

    override fun reset(venueId: Int, remarks: List<TeacherRemarkDbo>) = transaction {
        log.debug { "reset(venueId=$venueId, remarks.size=${remarks.size})" }
        require(remarks.all { it.venueId == venueId }) {
            "All teachers must have the same venueId: $venueId ($remarks))"
        }

        TeacherRemarksTable.deleteWhere { this.venueId eq venueId }

        val nextId = TeacherRemarksTable.nextId()
        remarks.mapIndexed { index, oldDbo ->
            val newDbo = oldDbo.copy(id = nextId + index)
            TeacherRemarksTable.insert {
                prepareInsert(it, newDbo)
            }
            newDbo
        }
    }

    private fun prepareInsert(stmt: InsertStatement<Number>, dbo: TeacherRemarkDbo) {
        stmt[TeacherRemarksTable.id] = dbo.id
        stmt[TeacherRemarksTable.venueId] = dbo.venueId
        stmt[TeacherRemarksTable.name] = dbo.name
        stmt[TeacherRemarksTable.rating] = dbo.rating
        stmt[TeacherRemarksTable.remark] = dbo.remark
    }
}
