package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.Plan

data class VenueDbo(
    val id: Int,
    val name: String,
    val slug: String,
    val facilities: String, // categories "," separated
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
    val planId: Int,
    val isFavorited: Boolean,
    val isWishlisted: Boolean,
    val isHidden: Boolean,
    val isDeleted: Boolean,
) {
    companion object {
        val dummy = VenueDbo(
            id = 999,
            name = "Josef Sutton",
            slug = "netus",
            facilities = "alienum",
            cityId = City.Amsterdam.id,
            officialWebsite = null,
            rating = 4,
            notes = "malorum",
            imageFileName = null,
            postalCode = "quem",
            street = "scripserit",
            addressLocality = "inimicus",
            latitude = "graece",
            longitude = "mus",
            description = "cetero",
            importantInfo = null,
            openingTimes = null,
            planId = Plan.UscPlan.Medium.id,
            isFavorited = false,
            isWishlisted = false,
            isHidden = false,
            isDeleted = false
        )
    }

    override fun toString() =
        "VenueDbo[id=$id, slug=$slug, name=$name, cityId=$cityId, imageFileName=$imageFileName, " + "isFavorited=$isFavorited, isWishlisted=$isWishlisted, isHidden=$isHidden, isDeleted=$isDeleted]"
}

interface VenueRepo {
    /** Doesn't do any filtering, not even the deleted ones. */
    fun selectAllAnywhere(): List<VenueDbo>
    fun selectAllByCity(cityId: Int): List<VenueDbo>
    fun insert(venue: VenueDbo): VenueDbo
    fun update(venue: VenueDbo): VenueDbo
    fun selectById(id: Int): VenueDbo?
    fun selectBySlug(slug: String): VenueDbo?
}

object VenuesTable : IntIdTable("VENUES", "ID") {
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
    val planId = integer("PLAN_ID") // custom
}

object ExposedVenueRepo : VenueRepo {

    private val log = logger {}

    override fun selectAllByCity(cityId: Int): List<VenueDbo> = transaction {
        VenuesTable.selectAll().where { VenuesTable.cityId.eq(cityId) }.map {
            VenueDbo.fromRow(it)
        }
    }

    override fun selectAllAnywhere(): List<VenueDbo> = transaction {
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
        val nextId =
            VenuesTable.select(VenuesTable.id).orderBy(VenuesTable.id, order = SortOrder.DESC).limit(1).toList()
                .firstOrNull()?.let {
                    it[VenuesTable.id].value + 1
                } ?: 1
        VenuesTable.insert {
            it[id] = nextId
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
            it[planId] = venue.planId
        }
        log.trace { "New venue ID=$nextId" }
        venue.copy(id = nextId)
    }

    override fun update(venue: VenueDbo): VenueDbo = transaction {
        log.debug { "Updating $venue" }
        val updated = VenuesTable.update(where = { VenuesTable.id.eq(venue.id) }) {
            it[notes] = venue.notes
            it[rating] = venue.rating
            it[imageFileName] = venue.imageFileName
            it[isHidden] = venue.isHidden
            it[isWishlisted] = venue.isWishlisted
            it[isFavorited] = venue.isFavorited
            it[isDeleted] = venue.isDeleted
            it[officialWebsite] = venue.officialWebsite
            it[street] = venue.street
            it[facilities] = venue.facilities
            it[postalCode] = venue.postalCode
            it[addressLocality] = venue.addressLocality
            it[longitude] = venue.longitude
            it[latitude] = venue.latitude
            it[description] = venue.description
            it[importantInfo] = venue.importantInfo
            it[openingTimes] = venue.openingTimes
            it[planId] = venue.planId
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
        planId = row[VenuesTable.planId],
    )
}

class InMemoryVenueRepo : VenueRepo {

    private var currentId = 1
    val stored = mutableMapOf<Int, VenueDbo>()

    override fun selectAllByCity(cityId: Int): List<VenueDbo> =
        stored.values.filter { it.cityId == cityId }.toList().sortedBy { it.id }

    override fun selectAllAnywhere(): List<VenueDbo> =
        stored.values.toList()

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
