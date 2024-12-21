package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.domain.Rating
import seepick.localsportsclub.api.domain.Venue
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenuesRepo
import seepick.localsportsclub.sync.SyncDispatcher
import java.net.URI

class VenuesService(
    private val venuesRepo: VenuesRepo,
    private val dispatcher: SyncDispatcher,
    private val baseUrl: String,
) {
    private val log = logger {}

    fun selectAll(): List<Venue> =
        venuesRepo.selectAll().filter { !it.isDeleted }.map { it.toVenue(baseUrl) }

    fun update(venue: Venue) {
        log.debug { "updating venue" }
        venuesRepo.update(venue.toDbo())
        dispatcher.dispatchVenueUpdated(venue)
    }
}

fun Venue.toDbo() = VenueDbo(
    id = id,
    name = name,
    slug = slug,
    facilities = facilities,
    cityId = city.id,
    officialWebsite = officialWebsite?.toString(),
    rating = rating.value,
    notes = notes,
    isFavorited = isFavorited,
    isWishlisted = isWishlisted,
    isHidden = isHidden,
    isDeleted = isDeleted,
)

fun VenueDbo.toVenue(baseUrl: String) = Venue(
    id = id,
    name = name,
    slug = slug,
    facilities = facilities,
    city = City.byId(cityId),
    rating = Rating.byValue(rating),
    notes = notes,
    officialWebsite = officialWebsite?.let { URI(it) },
    uscWebsite = URI("${baseUrl}/venues/$slug"),
    isFavorited = isFavorited,
    isWishlisted = isWishlisted,
    isHidden = isHidden,
    isDeleted = isDeleted,
)
