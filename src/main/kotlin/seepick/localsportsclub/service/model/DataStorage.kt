package seepick.localsportsclub.service.model

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url
import seepick.localsportsclub.api.City
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.sync.SyncDispatcher

class DataStorage(
    private val venueRepo: VenueRepo,
    private val venueLinksRepo: VenueLinksRepo,
    private val dispatcher: SyncDispatcher,
    private val baseUrl: String,
) {
    private val log = logger {}

    fun selectVenues(): List<Venue> {
        val allVenues = venueRepo.selectAll()
        // FIXME write test first for linking
//        val allVenuesById = allVenues.associateBy { it.id }
//        val allLinksById = venueLinksRepo.selectAll()
        val foo = allVenues.filter { !it.isDeleted }.map { it.toVenue(baseUrl) }
        return foo
    }

    fun update(venue: Venue) {
        log.debug { "updating venue" }
        venueRepo.update(venue.toDbo())
        dispatcher.dispatchVenueUpdated(venue)
    }
}

fun Venue.toDbo() = VenueDbo(
    id = id,
    name = name,
    slug = slug,
    facilities = facilities.joinToString(","),
    cityId = city.id,
    officialWebsite = officialWebsite?.toString(),
    rating = rating.value,
    notes = notes,
    description = description,
    longitude = longitude,
    latitude = latitude,
    street = street,
    addressLocality = addressLocality,
    postalCode = postalCode,
    importantInfo = importantInfo,
    openingTimes = openingTimes,
    imageFileName = imageFileName,
    isFavorited = isFavorited,
    isWishlisted = isWishlisted,
    isHidden = isHidden,
    isDeleted = isDeleted,
)

fun VenueDbo.toVenue(baseUrl: String) = Venue(
    id = id,
    name = name,
    slug = slug,
    facilities = facilities.split(","),
    city = City.byId(cityId),
    rating = Rating.byValue(rating),
    notes = notes,
    description = description,
    longitude = longitude,
    latitude = latitude,
    street = street,
    addressLocality = addressLocality,
    postalCode = postalCode,
    importantInfo = importantInfo,
    openingTimes = openingTimes,
    officialWebsite = officialWebsite?.let { Url(it) },
    uscWebsite = Url("${baseUrl}/venues/$slug"),
    isFavorited = isFavorited,
    isWishlisted = isWishlisted,
    isHidden = isHidden,
    imageFileName = imageFileName,
    isDeleted = isDeleted,
)
