package seepick.localsportsclub.service.model

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.api.City
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.DateTimeRange
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.SyncerListener

interface DataStorageListener {
    fun onVenueAdded(venue: Venue)
    fun onVenueUpdated(venue: Venue)
    fun onActivityAdded(activity: Activity)
    fun onFreetrainingAdded(freetraining: Freetraining)
}

object NoopDataStorageListener : DataStorageListener {
    override fun onVenueAdded(venue: Venue) {
    }

    override fun onVenueUpdated(venue: Venue) {
    }

    override fun onActivityAdded(activity: Activity) {
    }

    override fun onFreetrainingAdded(freetraining: Freetraining) {
    }
}

class DataStorage(
    private val venueRepo: VenueRepo,
    private val venueLinksRepo: VenueLinksRepo,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    uscConfig: UscConfig,
) : SyncerListener {

    private val baseUrl = uscConfig.baseUrl
    private val log = logger {}
    private val listeners = mutableListOf<DataStorageListener>()

    private val allActivitiesByVenueId: MutableMap<Int, MutableList<Activity>> by lazy {
        val simpleVenuesById = venueRepo.selectAll().map { it.toSimpleVenue() }.associateBy { it.id }
        activityRepo.selectAll().map { activityDbo ->
            val venueForActivity = simpleVenuesById[activityDbo.venueId]
            require(venueForActivity != null) { "Venue not found for: $activityDbo" }
            activityDbo.toActivity(venueForActivity)
        }.groupByTo(mutableMapOf()) { it.venue.id }
    }

    private val allFretrainingsByVenueId: MutableMap<Int, MutableList<Freetraining>> by lazy {
        val venuesById = venueRepo.selectAll().map { it.toSimpleVenue() }.associateBy { it.id }
        freetrainingRepo.selectAll().map { freetrainingDbo ->
            val venueForFreetraining = venuesById[freetrainingDbo.venueId]
            require(venueForFreetraining != null) { "Venue not found for: $freetrainingDbo" }
            freetrainingDbo.toFreetraining(venueForFreetraining)
        }.groupByTo(mutableMapOf()) { it.venue.id }
    }

    private val allVenues: MutableList<Venue> by lazy {
        val allVenueDbos = venueRepo.selectAll()
        // FIXME implement venue linking (test first!)
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

    // invoked on startup
    fun selectAllVenues(): List<Venue> {
        return allVenues
    }

    fun selectAllActivities(): List<Activity> =
        allActivitiesByVenueId.values.toList().flatten()

    fun selectVenueById(id: Int) =
        allVenues.single { it.id == id }

    fun selectAllFreetrainings(): List<Freetraining> =
        allFretrainingsByVenueId.values.toList().flatten()

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
            ?: error("Failed to add activity! Could not find venue by ID for: $activityDbo")
        val activity = activityDbo.toActivity(venue)
        venue.activities += activity
        allActivitiesByVenueId.getOrPut(activity.venue.id) { mutableListOf() }.add(activity)
        dispatchOnActivityAdded(activity)
    }

    override fun onFreetrainingDboAdded(freetrainingDbo: FreetrainingDbo) {
        val venue = allVenues.firstOrNull { it.id == freetrainingDbo.venueId }
            ?: error("Failed to add freetraining! Could not find venue by ID for: $freetrainingDbo")
        val freetraining = freetrainingDbo.toFreetraining(venue)
        venue.freetrainings += freetraining
        allFretrainingsByVenueId.getOrPut(freetraining.venue.id) { mutableListOf() }.add(freetraining)
        dispatchOnFreetrainingAdded(freetraining)
    }

    override fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        val stored = allActivitiesByVenueId[activityDbo.venueId]!!.single { it.id == activityDbo.id }
        when (field) {
            ActivityFieldUpdate.IsBooked -> stored.isBooked = activityDbo.isBooked
            ActivityFieldUpdate.WasCheckedin -> stored.wasCheckedin = activityDbo.wasCheckedin
        }
    }

    fun update(venue: Venue) {
        log.debug { "updating $venue" }
        venueRepo.update(venue.toDbo())
        allActivitiesByVenueId[venue.id]?.forEach { activity ->
            activity.venue.updateSelfBy(venue)
        }
        dispatchOnVenueUpdated(venue)
    }

    private fun dispatchOnVenueAdded(venue: Venue) {
        listeners.forEach {
            it.onVenueAdded(venue)
        }
    }

    private fun dispatchOnVenueUpdated(venue: Venue) {
        listeners.forEach {
            it.onVenueUpdated(venue)
        }
    }

    private fun dispatchOnActivityAdded(activity: Activity) {
        listeners.forEach {
            it.onActivityAdded(activity)
        }
    }

    private fun dispatchOnFreetrainingAdded(freetraining: Freetraining) {
        listeners.forEach {
            it.onFreetrainingAdded(freetraining)
        }
    }
}

fun ActivityDbo.toActivity(venue: SimpleVenue) = Activity(
    id = id,
    venue = venue,
    name = name,
    category = category,
    spotsLeft = spotsLeft,
    dateTimeRange = DateTimeRange(from, to),
    isBooked = isBooked,
    wasCheckedin = wasCheckedin,
)

fun FreetrainingDbo.toFreetraining(venue: SimpleVenue) = Freetraining(
    id = id,
    venue = venue,
    name = name,
    category = category,
    date = date,
    checkedinTime = checkedinTime,
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
    officialWebsite = officialWebsite,
    uscWebsite = "${baseUrl}/venues/$slug",
    isFavorited = isFavorited,
    isWishlisted = isWishlisted,
    isHidden = isHidden,
    imageFileName = imageFileName,
    isDeleted = isDeleted,
)
