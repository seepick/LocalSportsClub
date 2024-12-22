package seepick.localsportsclub.service.model

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url
import seepick.localsportsclub.api.City
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.sync.SyncDispatcher

class DataStorage(
    private val venueRepo: VenueRepo,
    private val venueLinksRepo: VenueLinksRepo,
    private val activityRepo: ActivityRepo,
    private val dispatcher: SyncDispatcher,
    private val baseUrl: String,
) {
    private val log = logger {}

    private val allActivitiesByVenueId by lazy {
        val venuesById = venueRepo.selectAll().associateBy { it.id }
        activityRepo.selectAll().mapNotNull { activityDbo ->
            val venueForActivity = venuesById[activityDbo.venueId]
            if (venueForActivity == null) {
                log.error { "Venue not found for activity: $activityDbo" }
                null
            } else {
                activityDbo.toActivity(venueForActivity)
            }
        }.associateBy { it.venue.id }.toMutableMap()
    }

    private val allVenues: MutableList<Venue> by lazy {
        val allVenueDbos = venueRepo.selectAll()
        // FIXME write test first for linking
//        val allVenuesById = allVenues.associateBy { it.id }
//        val allLinksById = venueLinksRepo.selectAll()
        allVenueDbos
            .filter { !it.isDeleted }
            .map { venueDbo ->
                venueDbo.toVenue(baseUrl).also { venue ->
                    allActivitiesByVenueId[venue.id]?.also { activitiesForVenue ->
                        venue.activities += activitiesForVenue
                    }
                }
            }.toMutableList()
    }

    // invoked on startup
    fun selectAllVenues(): List<Venue> {
        return allVenues
    }

    fun onVenueDboAdded(venueDbo: VenueDbo) {
        log.debug { "onVenueAdded($venueDbo)" }
        val venue = venueDbo.toVenue(baseUrl)
        allActivitiesByVenueId[venue.id]?.also { activitiesForVenue ->
            venue.activities += activitiesForVenue
        }

        allVenues += venue
        dispatcher.dispatchVenueAdded(venue)
    }

    fun onActivityDboAdded(activityDbo: ActivityDbo) {
        val venue = allVenues.first { it.id == activityDbo.venueId }
        val activity = activityDbo.toActivity(venue)
        venue.activities += activity
//        dispatcher.dispatchActivityAdded(activity) // FIXME
    }

    fun update(venue: Venue) {
        log.debug { "updating $venue" }
        venueRepo.update(venue.toDbo())
    }
}

fun ActivityDbo.toActivity(venue: SimpleVenue) = Activity(
    id = id,
    venue = venue,
    name = name,
    category = category,
    spotsLeft = spotsLeft,
    from = from,
    to = to,
)

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
