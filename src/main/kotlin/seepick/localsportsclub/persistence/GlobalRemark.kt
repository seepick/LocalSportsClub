package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

object GlobalRemarkDboTable : IntIdTable("GLOBAL_REMARKS", "ID") {
    val type = enumerationByName<GlobalRemarkType>("TYPE", 32)
    val name = varchar("NAME", 128)
    val rating = enumerationByName<RemarkDboRating>("RATING", 32)
    val remark = text("REMARK")
}

data class GlobalRemarkDbo(
    val id: Int,
    val type: GlobalRemarkType,
    val name: String,
    val rating: RemarkDboRating,
    val remark: String,
) {
    companion object {
        fun fromRow(row: ResultRow) = GlobalRemarkDbo(
            id = row[GlobalRemarkDboTable.id].value,
            type = row[GlobalRemarkDboTable.type],
            name = row[GlobalRemarkDboTable.name],
            rating = row[GlobalRemarkDboTable.rating],
            remark = row[GlobalRemarkDboTable.remark],
        )
    }
}

enum class GlobalRemarkType {
    Category,
    Activity,
    Teacher,
}

interface GlobalRemarkRepository {
    fun selectAll(): List<GlobalRemarkDbo>
    fun insert(remarks: List<GlobalRemarkDbo>): List<GlobalRemarkDbo>
    fun update(remarks: List<GlobalRemarkDbo>)
    fun delete(remarks: List<GlobalRemarkDbo>)
}

object GlobalRemarkExposedRepository : GlobalRemarkRepository {

    private val log = logger {}

    override fun selectAll(): List<GlobalRemarkDbo> = transaction {
        GlobalRemarkDboTable.selectAll().map {
            GlobalRemarkDbo.fromRow(it)
        }
    }

    override fun insert(remarks: List<GlobalRemarkDbo>): List<GlobalRemarkDbo> = transaction {
        log.debug { "insert: $remarks" }

        val nextId = GlobalRemarkDboTable.nextId()
        remarks.mapIndexed { index, oldDbo ->
            val newDbo = oldDbo.copy(id = nextId + index)
            GlobalRemarkDboTable.insert {
                prepareStmt(it, newDbo)
            }
            newDbo
        }
    }

    private fun prepareStmt(stmt: UpdateBuilder<Number>, dbo: GlobalRemarkDbo) {
        stmt[GlobalRemarkDboTable.id] = dbo.id
        stmt[GlobalRemarkDboTable.type] = dbo.type
        stmt[GlobalRemarkDboTable.name] = dbo.name
        stmt[GlobalRemarkDboTable.rating] = dbo.rating
        stmt[GlobalRemarkDboTable.remark] = dbo.remark
    }

    override fun update(remarks: List<GlobalRemarkDbo>): Unit = transaction {
        log.debug { "update: $remarks" }
        remarks.forEach { remark ->
            GlobalRemarkDboTable.update(
                where = { GlobalRemarkDboTable.id.eq(remark.id) }
            ) {
                it[GlobalRemarkDboTable.name] = remark.name
                it[GlobalRemarkDboTable.rating] = remark.rating
                it[GlobalRemarkDboTable.remark] = remark.remark
            }
        }
    }

    override fun delete(remarks: List<GlobalRemarkDbo>): Unit = transaction {
        log.debug { "delete: $remarks" }
        val ids = remarks.map { it.id }
        GlobalRemarkDboTable.deleteWhere {
            this.id inList ids
        }
    }
}
