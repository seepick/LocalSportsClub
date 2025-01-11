package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Sequence
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.nextIntVal
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

data class VenueDbo(
    val id: Int,
    val name: String,
    val slug: String,
    val facilities: String, // "," separated
    /** @see [seepick.localsportsclub.api.City] */
    val cityId: Int,
    val officialWebsite: String?,
    val rating: Int,
    val notes: String,
    val imageFileName: String?,
    val postalCode: String,
    val street: String,
    val addressLocality: String,
    val latitude: String,
    val longitude: String,
    val description: String,
    val importantInfo: String?,
    val openingTimes: String?,
    val isFavorited: Boolean,
    val isWishlisted: Boolean,
    val isHidden: Boolean,
    val isDeleted: Boolean,
) {
    companion object; // for extensions

    override fun toString() =
        "VenueDbo[id=$id, slug=$slug, name=$name, imageFileName=$imageFileName, " + "isFavorited=$isFavorited, isWishlisted=$isWishlisted, isHidden=$isHidden, isDeleted=$isDeleted]"
}

interface VenueRepo {
    /** Doesn't do any filtering, not even the deleted ones. */
    fun selectAll(): List<VenueDbo>
    fun insert(venue: VenueDbo): VenueDbo
    fun update(venue: VenueDbo): VenueDbo
    fun selectById(id: Int): VenueDbo?
    fun selectBySlug(slug: String): VenueDbo?
}

object VenuesTable : IntIdTable("PUBLIC.VENUES", "ID") {
    val name = varchar("NAME", 256) // sync details
    val slug = varchar("SLUG", 64).uniqueIndex("VENUES_SLUG_UNIQUE_INDEX") // sync details
    val facilities = text("FACILITIES") // sync details; aka disciplines; comma separated list
    val cityId = integer("CITY_ID") // usc config
    val officialWebsite = varchar("OFFICIAL_WEBSITE", 256).nullable() // sync details
    val rating = integer("RATING") // custom
    val notes = text("NOTES") // custom
    val postalCode = varchar("POSTAL_CODE", 64) // sync details
    val street = varchar("STREET", 128) // sync details
    val addressLocality = varchar("ADDRESS_LOCALITY", 128) // sync details; "Amsterdam, Netherlands"
    val latitude = varchar("LATITUDE", 16) // sync details
    val longitude = varchar("LONGITUDE", 16) // sync details
    val imageFileName = text("IMAGE_FILE_NAME").nullable() // custom
    val description = text("DESCRIPTION") // sync details
    val importantInfo = text("IMPORTANT_INFO").nullable() // sync details
    val openingTimes = text("OPENING_TIMES").nullable() // sync details
    val isFavorited = bool("IS_FAVORITED") // custom
    val isWishlisted = bool("IS_WISHLISTED") // custom
    val isHidden = bool("IS_HIDDEN") // custom
    val isDeleted = bool("IS_DELETED") // custom
}

object ExposedVenueRepo : VenueRepo {

    val idSequence = Sequence("SEQ_VENUES_ID")

    private val log = logger {}

    override fun selectAll(): List<VenueDbo> = transaction {
        VenuesTable.selectAll().map {
            VenueDbo.fromRow(it)
        }
    }

    override fun selectById(id: Int): VenueDbo? = transaction {
        VenuesTable.selectAll().where { VenuesTable.id.eq(id) }.map {
            VenueDbo.fromRow(it)
        }.singleOrNull()
    }

    override fun selectBySlug(slug: String): VenueDbo? = transaction {
        VenuesTable.selectAll().where { VenuesTable.slug.eq(slug) }.map {
            VenueDbo.fromRow(it)
        }.singleOrNull()
    }

    override fun insert(venue: VenueDbo): VenueDbo = transaction {
        log.debug { "Inserting $venue" }
        val nextSeq = idSequence.nextIntVal()
        val nextId = VenuesTable.insertAndGetId {
            it[id] = nextSeq
            it[name] = venue.name
            it[slug] = venue.slug
            it[notes] = venue.notes
            it[facilities] = venue.facilities
            it[cityId] = venue.cityId
            it[officialWebsite] = venue.officialWebsite
            it[rating] = venue.rating
            it[street] = venue.street
            it[addressLocality] = venue.addressLocality
            it[postalCode] = venue.postalCode
            it[longitude] = venue.longitude
            it[latitude] = venue.latitude
            it[description] = venue.description
            it[imageFileName] = venue.imageFileName
            it[openingTimes] = venue.openingTimes
            it[importantInfo] = venue.importantInfo
            it[isFavorited] = venue.isFavorited
            it[isWishlisted] = venue.isWishlisted
            it[isHidden] = venue.isHidden
            it[isDeleted] = venue.isDeleted
        }.value
        venue.copy(id = nextId)
    }

    override fun update(venue: VenueDbo): VenueDbo = transaction {
        val updated = VenuesTable.update(where = { VenuesTable.id.eq(venue.id) }) {
            it[notes] = venue.notes
            it[rating] = venue.rating
            it[imageFileName] = venue.imageFileName
            it[isHidden] = venue.isHidden
            it[isWishlisted] = venue.isWishlisted
            it[isFavorited] = venue.isFavorited
            it[isDeleted] = venue.isDeleted
            it[officialWebsite] = venue.officialWebsite
        }
        if (updated != 1) error("Expected 1 to be updated by ID ${venue.id}, but was: $updated")
        venue
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
        postalCode = row[VenuesTable.postalCode],
        street = row[VenuesTable.street],
        addressLocality = row[VenuesTable.addressLocality],
        longitude = row[VenuesTable.longitude],
        latitude = row[VenuesTable.latitude],
        description = row[VenuesTable.description],
        importantInfo = row[VenuesTable.importantInfo],
        imageFileName = row[VenuesTable.imageFileName],
        openingTimes = row[VenuesTable.openingTimes],
        isFavorited = row[VenuesTable.isFavorited],
        isWishlisted = row[VenuesTable.isWishlisted],
        isHidden = row[VenuesTable.isHidden],
        isDeleted = row[VenuesTable.isDeleted],
    )
}

class InMemoryVenueRepo : VenueRepo {

    private var currentId = 1
    val stored = mutableMapOf<Int, VenueDbo>()

    override fun selectAll(): List<VenueDbo> = stored.values.toList().sortedBy { it.id }

    override fun selectBySlug(slug: String): VenueDbo? = stored.values.firstOrNull { it.slug == slug }

    override fun selectById(id: Int): VenueDbo? = stored.values.firstOrNull { it.id == id }

    override fun insert(venue: VenueDbo): VenueDbo {
        val newVenue = venue.copy(id = currentId++)
        require(stored.values.none { it.slug == venue.slug })
        stored[newVenue.id] = newVenue
        return newVenue
    }

    override fun update(venue: VenueDbo): VenueDbo {
        require(stored[venue.id] != null)
        stored[venue.id] = venue
        return venue
    }
}
