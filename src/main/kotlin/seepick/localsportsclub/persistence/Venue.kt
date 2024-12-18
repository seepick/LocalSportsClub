package seepick.localsportsclub.persistence

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

data class VenueDbo(
    val id: Int,
    val name: String,
    val slug: String,
    val facilities: String,
    /** @see [seepick.localsportsclub.api.City] */
    val cityId: Int,
    val officialWebsite: String?,
    val rating: Int,
    val note: String,
    val isFavorited: Boolean,
    val isWishlisted: Boolean,
    val isHidden: Boolean,
    val isDeleted: Boolean,
) {
    companion object // for extensions
}

interface VenuesRepo {
    fun selectAll(): List<VenueDbo>
    fun persist(venues: List<VenueDbo>)
}

object VenuesTable : IntIdTable("PUBLIC.VENUES", "ID") {
    val name = varchar("NAME", 256) // sync list
    val slug = varchar("SLUG", 64).uniqueIndex("VENUES_SLUG_UNIQUE_INDEX") // sync list
    val facilities = text("FACILITIES") // sync list; comma separated list

    val cityId = integer("CITY_ID") // usc config

    val officialWebsite = varchar("OFFICIAL_WEBSITE", 256).nullable() // sync details

    val rating = integer("RATING") // custom
    val note = text("NOTE") // custom
    val isFavorited = bool("IS_FAVORITED") // custom
    val isWishlisted = bool("IS_WISHLISTED") // custom
    val isHidden = bool("IS_HIDDEN") // custom
    val isDeleted = bool("IS_DELETED") // custom
}

// a.k.a. "other locations"
object VenueLinksTable : Table("PUBLIC.VENUE_LINKS") {
    val venue1Id = reference("VENUE1_ID", VenuesTable)
    val venue2Id = reference("VENUE2_ID", VenuesTable)
    override val primaryKey = PrimaryKey(venue1Id, venue2Id, name = "PK_VENUE_LINKS")

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
        note = row[VenuesTable.note],
        facilities = row[VenuesTable.facilities],
        cityId = row[VenuesTable.cityId],
        officialWebsite = row[VenuesTable.officialWebsite],
        rating = row[VenuesTable.rating],
        isFavorited = row[VenuesTable.isFavorited],
        isWishlisted = row[VenuesTable.isWishlisted],
        isHidden = row[VenuesTable.isHidden],
        isDeleted = row[VenuesTable.isDeleted],
    )

    private fun VenueDbo.insert(id: Int) {
        val p = this // resolve name shadowing
        VenuesTable.insert {
            it[VenuesTable.id] = EntityID(id, VenuesTable)
            it[name] = p.name
            it[slug] = p.slug
            it[note] = p.note
            it[facilities] = p.facilities
            it[cityId] = p.cityId
            it[officialWebsite] = p.officialWebsite
            it[rating] = p.rating
            it[isFavorited] = p.isFavorited
            it[isWishlisted] = p.isWishlisted
            it[isHidden] = p.isHidden
            it[isDeleted] = p.isDeleted
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
