package seepick.localsportsclub.persistence

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

data class PartnerDbo(
    val id: Int,
    val name: String,
) {
    companion object // for extensions
}

//class PartnerEntity(id: EntityID<Int>) : IntEntity(id) {
//    companion object : IntEntityClass<PartnerEntity>(PartnersTable)
//    var name by PartnersTable.name
//}

interface PartnersRepo {
    fun loadAll(): List<PartnerDbo>
    fun persist(partner: PartnerDbo)
}

object PartnersTable : IntIdTable("PUBLIC.PARTNERS", "ID") {
    val name = varchar("NAME", 256)
}

class ExposedPartnersRepo : PartnersRepo {
    override fun loadAll(): List<PartnerDbo> =
        PartnersTable.selectAll().map {
            PartnerDbo.fromRow(it)
        }

    override fun persist(partner: PartnerDbo) {
        partner.insert()
    }

    private fun PartnerDbo.Companion.fromRow(row: ResultRow) = PartnerDbo(
        id = row[PartnersTable.id].value,
        name = row[PartnersTable.name],
    )

    private fun PartnerDbo.insert() {
        val p = this // resolve name shadowing
        PartnersTable.insert {
            it[PartnersTable.id] = EntityID(p.id, PartnersTable)
            it[name] = p.name
        }
    }
}

class InMemoryPartnersRepo : PartnersRepo {
    val partners = mutableMapOf<Int, PartnerDbo>()

    override fun loadAll(): List<PartnerDbo> =
        partners.values.toList().sortedBy { it.id }
    override fun persist(partner: PartnerDbo) {
        partners[partner.id] = partner
    }
}
