package seepick.localsportsclub.persistence

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

data class VenueDbo(
    val id: Int,
    val name: String,
    val slug: String,
) {
    companion object // for extensions
}

interface VenuesRepo {
    fun selectAll(): List<VenueDbo>
    fun persist(venues: List<VenueDbo>)
}

object VenuesTable : IntIdTable("PUBLIC.VENUES", "ID") {
    val name = varchar("NAME", 256)
    val slug = varchar("SLUG", 64).uniqueIndex("VENUES_SLUG_UNIQUE_INDEX")
}

object ExposedVenuesRepo : VenuesRepo {

    override fun selectAll(): List<VenueDbo> = transaction {
        VenuesTable.selectAll().map {
            VenueDbo.fromRow(it)
        }
    }

    override fun persist(venues: List<VenueDbo>) {
        if (venues.isEmpty()) return
        transaction {
            var nextId = (selectAll().maxOfOrNull { it.id } ?: 0) + 1
            venues.forEach { venue ->
                venue.insert(nextId++)
            }
        }
    }

    private fun VenueDbo.Companion.fromRow(row: ResultRow) = VenueDbo(
        id = row[VenuesTable.id].value,
        name = row[VenuesTable.name],
        slug = row[VenuesTable.slug],
    )

    private fun VenueDbo.insert(id: Int) {
        val p = this // resolve name shadowing
        VenuesTable.insert {
            it[VenuesTable.id] = EntityID(id, VenuesTable)
            it[name] = p.name
            it[slug] = p.slug
        }
    }
}

class InMemoryVenuesRepo : VenuesRepo {
    val stored = mutableMapOf<Int, VenueDbo>()

    override fun selectAll(): List<VenueDbo> =
        stored.values.toList().sortedBy { it.id }

    override fun persist(venues: List<VenueDbo>) {
        venues.forEach { venue ->
            stored[venue.id] = venue
        }
    }
}
