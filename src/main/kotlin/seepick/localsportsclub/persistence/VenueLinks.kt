package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

// a.k.a. "other locations"
object VenueLinkDboTable : Table("VENUE_LINKS") {
    val venue1Id = reference("VENUE1_ID", VenueDboTable)
    val venue2Id = reference("VENUE2_ID", VenueDboTable)
    override val primaryKey = PrimaryKey(venue1Id, venue2Id, name = "PK_VENUE_LINKS")
}

interface VenueLinksRepo {
    fun selectAll(cityId: Int): List<VenueIdLink>
    fun insert(venueIdLink: VenueIdLink)
}

data class VenueIdLink(
    val id1: Int, val id2: Int,
) {
    init {
        require(id1 != id2) { "id1[$id1] == id2[$id2]" }
    }

    override fun toString() = "VenueIdLink[$id1/$id2]"
    override fun hashCode() = id1.hashCode() + id2.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is VenueIdLink) return false
        return (id1 == other.id1 && id2 == other.id2) || (id1 == other.id2 && id2 == other.id1)
    }
}

object ExposedVenueLinksRepo : VenueLinksRepo {

    private val log = logger {}

    override fun selectAll(cityId: Int): List<VenueIdLink> = transaction {
        log.debug { "selectAll(cityId=$cityId)" }
        val venue1Alias = VenueDboTable.alias("v1")
        val venue2Alias = VenueDboTable.alias("v2")
        VenueLinkDboTable.join(
            venue1Alias,
            JoinType.LEFT,
            onColumn = VenueLinkDboTable.venue1Id,
            otherColumn = venue1Alias[VenueDboTable.id]
        ).join(
            venue2Alias,
            JoinType.LEFT,
            onColumn = VenueLinkDboTable.venue2Id,
            otherColumn = venue2Alias[VenueDboTable.id]
        ).selectAll().where {
            (venue1Alias[VenueDboTable.cityId] eq cityId) and (venue2Alias[VenueDboTable.cityId] eq cityId)
        }.map {
            VenueIdLink(
                it[VenueLinkDboTable.venue1Id].value,
                it[VenueLinkDboTable.venue2Id].value,
            )
        }
    }

    override fun insert(venueIdLink: VenueIdLink): Unit = transaction {
        log.debug { "insert($venueIdLink)" }
        val (id1, id2) = if (venueIdLink.id1 < venueIdLink.id2) venueIdLink.id1 to venueIdLink.id2 else venueIdLink.id2 to venueIdLink.id1
        VenueLinkDboTable.insert {
            it[venue1Id] = id1
            it[venue2Id] = id2
        }
    }
}
