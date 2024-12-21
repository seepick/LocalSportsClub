package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

data class VenueDbo(
    val id: Int,
    val name: String,
    val slug: String,
    val facilities: String,
    /** @see [seepick.localsportsclub.api.City] */
    val cityId: Int,
    val officialWebsite: String?,
    val rating: Int,
    val notes: String,
    val isFavorited: Boolean,
    val isWishlisted: Boolean,
    val isHidden: Boolean,
    val isDeleted: Boolean,
) {
    companion object // for extensions
}

interface VenuesRepo {
    fun selectAll(): List<VenueDbo> // FIXME filter out isDeleted
    fun insert(venue: VenueDbo): VenueDbo
    fun update(venue: VenueDbo)
}

object VenuesTable : IntIdTable("PUBLIC.VENUES", "ID") {
    val name = varchar("NAME", 256) // sync list
    val slug = varchar("SLUG", 64).uniqueIndex("VENUES_SLUG_UNIQUE_INDEX") // sync list
    val facilities = text("FACILITIES") // sync list; comma separated list
    val cityId = integer("CITY_ID") // usc config
    val officialWebsite = varchar("OFFICIAL_WEBSITE", 256).nullable() // sync details
    val rating = integer("RATING") // custom
    val notes = text("NOTES") // custom
    val isFavorited = bool("IS_FAVORITED") // custom
    val isWishlisted = bool("IS_WISHLISTED") // custom
    val isHidden = bool("IS_HIDDEN") // custom
    val isDeleted = bool("IS_DELETED") // custom
}

object ExposedVenuesRepo : VenuesRepo {

    private val log = logger {}

    override fun selectAll(): List<VenueDbo> = transaction {
        VenuesTable.selectAll().map {
            VenueDbo.fromRow(it)
        }
    }

    override fun insert(venue: VenueDbo): VenueDbo =
        transaction {
            log.debug { "Inserting venue: $venue" }
            // TODO ask DBMS for sequence
            val nextId = (selectAll().maxOfOrNull { it.id } ?: 0) + 1
            venue.insertSelf(nextId)
            venue.copy(id = nextId)
        }

    override fun update(venue: VenueDbo) {
        transaction {
            val updated = VenuesTable.update(where = { VenuesTable.id.eq(venue.id) }) {
                it[notes] = venue.notes
                it[rating] = venue.rating
            }
            if (updated != 1) error("Expected 1 to be updated by ID $venue.id, but was: $updated")
        }
    }

    private fun VenueDbo.Companion.fromRow(row: ResultRow) = VenueDbo(
        id = row[VenuesTable.id].value,
        name = row[VenuesTable.name],
        slug = row[VenuesTable.slug],
        notes = row[VenuesTable.notes],
        facilities = row[VenuesTable.facilities],
        cityId = row[VenuesTable.cityId],
        officialWebsite = row[VenuesTable.officialWebsite],
        rating = row[VenuesTable.rating],
        isFavorited = row[VenuesTable.isFavorited],
        isWishlisted = row[VenuesTable.isWishlisted],
        isHidden = row[VenuesTable.isHidden],
        isDeleted = row[VenuesTable.isDeleted],
    )

    private fun VenueDbo.insertSelf(id: Int) {
        val p = this // resolve name shadowing
        VenuesTable.insert {
            it[VenuesTable.id] = EntityID(id, VenuesTable)
            it[name] = p.name
            it[slug] = p.slug
            it[notes] = p.notes
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
    private var currentId = 1
    val stored = mutableMapOf<Int, VenueDbo>()

    override fun selectAll(): List<VenueDbo> =
        stored.values.toList().sortedBy { it.id }

    override fun insert(venue: VenueDbo): VenueDbo {
        val newVenue = venue.copy(id = currentId++)
        stored[newVenue.id] = newVenue
        return newVenue
    }

    override fun update(venue: VenueDbo) {
        require(stored[venue.id] != null)
        stored[venue.id] = venue
    }
}
