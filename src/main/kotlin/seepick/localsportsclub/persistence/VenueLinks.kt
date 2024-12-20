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
    fun selectAll(): List<Pair<Int, Int>>
    fun insert(venueId1: Int, venueId2: Int)
}

object ExposedVenueLinksRepo : VenueLinksRepo {

    private val log = logger {}

    override fun selectAll(): List<Pair<Int, Int>> =
        transaction {
            VenueLinksTable.selectAll().map {
                it[VenueLinksTable.venue1Id].value to it[VenueLinksTable.venue2Id].value
            }
        }


    override fun insert(venueId1: Int, venueId2: Int) {
        log.debug { "adding venue links for: $venueId1 and $venueId2" }
        if (venueId1 == venueId2) error("Venue can't reference itself!")
        transaction {
            VenueLinksTable.insert {
                it[venue1Id] = venueId1
                it[venue2Id] = venueId2
            }
        }
    }
}

class InMemoryVenueLinksRepo : VenueLinksRepo {
    val stored = mutableMapOf<Int, Int>()
    override fun selectAll(): List<Pair<Int, Int>> =
        stored.toList()

    override fun insert(venueId1: Int, venueId2: Int) {
        stored += venueId1 to venueId2
    }

}
