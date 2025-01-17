package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

// a.k.a. "other locations"
object VenueLinksTable : Table("PUBLIC.VENUE_LINKS") {
    val venue1Id = reference("VENUE1_ID", VenuesTable)
    val venue2Id = reference("VENUE2_ID", VenuesTable)
    override val primaryKey = PrimaryKey(venue1Id, venue2Id, name = "PK_VENUE_LINKS")
}

interface VenueLinksRepo {
    fun selectAll(): List<VenueIdLink>
    fun insert(venueIdLink: VenueIdLink)
}

data class VenueIdLink(
    val id1: Int, val id2: Int
) {
    init {
        require(id1 != id2) { "id1[$id1] == id2[$id2]" }
    }

    override fun toString() = "VenueIdLink[$id1/$id2]"
    override fun hashCode() = id1.hashCode() + id2.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is VenueIdLink) return false
        return (id1 == other.id1 && id2 == other.id2) ||
                (id1 == other.id2 && id2 == other.id1)
    }
}

object ExposedVenueLinksRepo : VenueLinksRepo {

    private val log = logger {}

    override fun selectAll(): List<VenueIdLink> = transaction {
        VenueLinksTable.selectAll().map {
            VenueIdLink(
                it[VenueLinksTable.venue1Id].value,
                it[VenueLinksTable.venue2Id].value,
            )
        }
    }

    override fun insert(venueIdLink: VenueIdLink): Unit = transaction {
        if (selectAll().toSet().contains(venueIdLink)) {
            error("Duplicate venue link: $venueIdLink")
        }
        VenueLinksTable.insert {
            it[venue1Id] = venueIdLink.id1
            it[venue2Id] = venueIdLink.id2
        }
    }
}

class InMemoryVenueLinksRepo : VenueLinksRepo {
    val stored = mutableSetOf<VenueIdLink>()
    override fun selectAll(): List<VenueIdLink> =
        stored.toList()

    override fun insert(venueIdLink: VenueIdLink) {
        stored += venueIdLink
    }

}
