package seepick.localsportsclub.service.model

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.api.City
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.DateTimeRange
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.SyncerListener

interface DataStorageListener {
    fun onVenueAdded(venue: Venue)
}

class DataStorage(
    private val venueRepo: VenueRepo,
    private val venueLinksRepo: VenueLinksRepo,
    private val activityRepo: ActivityRepo,
    uscConfig: UscConfig,
) : SyncerListener {

    private val baseUrl = uscConfig.baseUrl
    private val log = logger {}
    private val listeners = mutableListOf<DataStorageListener>()

    private val allActivitiesByVenueId: MutableMap<Int, MutableList<Activity>> by lazy {
        val venuesById = venueRepo.selectAll().map { it.toSimpleVenue() }.associateBy { it.id }
        activityRepo.selectAll().map { activityDbo ->
            val venueForActivity = venuesById[activityDbo.venueId]
            require(venueForActivity != null) { "Venue not found for activity: $activityDbo" }
            activityDbo.toActivity(venueForActivity)
        }.groupByTo(mutableMapOf()) { it.venue.id }
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

    fun registerListener(listener: DataStorageListener) {
        listeners += listener
    }

    fun dispatchOnVenueAdded(venue: Venue) {
        listeners.forEach {
            it.onVenueAdded(venue)
        }
    }

    // invoked on startup
    fun selectAllVenues(): List<Venue> {
        return allVenues
    }

    override fun onVenueDboAdded(venueDbo: VenueDbo) {
        log.debug { "onVenueAdded($venueDbo)" }
        val venue = venueDbo.toVenue(baseUrl)
        // no, not possible
//        allActivitiesByVenueId[venue.id]?.also { activitiesForVenue ->
//            venue.activities += activitiesForVenue
//        }
        allVenues += venue
        dispatchOnVenueAdded(venue)
    }

    override fun onActivityDboAdded(activityDbo: ActivityDbo) {
        val venue = allVenues.firstOrNull { it.id == activityDbo.venueId }
            ?: error("Could not find venue for: $activityDbo\nVenues: $allVenues")
        val activity = activityDbo.toActivity(venue)
        venue.activities += activity
        allActivitiesByVenueId.getOrPut(activityDbo.venueId) { mutableListOf() }.add(activity)
//        dispatcher.dispatchActivityAdded(activity) // FIXME
    }

    override fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        val stored = allActivitiesByVenueId[activityDbo.venueId]!!.single { it.id == activityDbo.id }
        when (field) {
            ActivityFieldUpdate.Scheduled -> stored.isBooked = activityDbo.isBooked
        }
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
    dateTimeRange = DateTimeRange(from, to),
    isBooked = isBooked
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

fun VenueDbo.toVenue(baseUrl: Url) = Venue(
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
