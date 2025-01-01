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
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.FreetrainingFieldUpdate
import seepick.localsportsclub.sync.SyncerListener

interface DataStorageListener {
    fun onVenueAdded(venue: Venue)
    fun onVenueUpdated(venue: Venue)
    fun onActivitiesAdded(activities: List<Activity>)
    fun onFreetrainingsAdded(freetrainings: List<Freetraining>)
}

object NoopDataStorageListener : DataStorageListener {
    override fun onVenueAdded(venue: Venue) {
    }

    override fun onVenueUpdated(venue: Venue) {
    }

    override fun onActivitiesAdded(activities: List<Activity>) {
    }

    override fun onFreetrainingsAdded(freetrainings: List<Freetraining>) {
    }
}

class DataStorage(
    private val venueRepo: VenueRepo,
    private val venueLinksRepo: VenueLinksRepo,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val clock: Clock,
    uscConfig: UscConfig,
) : SyncerListener {

    private val log = logger {}
    private val baseUrl = uscConfig.baseUrl
    private val listeners = mutableListOf<DataStorageListener>()

    private val simpleVenuesById by lazy {
        venueRepo.selectAll().map { it.toSimpleVenue() }.associateBy { it.id }
    }

    private val allActivitiesByVenueId: MutableMap<Int, MutableList<Activity>> by lazy {
        activityRepo.selectAllUpcoming(clock.today()).map { activityDbo ->
            val venueForActivity = simpleVenuesById[activityDbo.venueId] ?: error("Venue not found for: $activityDbo")
            activityDbo.toActivity(venueForActivity)
        }.groupByTo(mutableMapOf()) { it.venue.id }
    }

    private val allFreetrainingsByVenueId: MutableMap<Int, MutableList<Freetraining>> by lazy {
        freetrainingRepo.selectAllUpcoming(clock.today()).map { freetrainingDbo ->
            val venueForFreetraining =
                simpleVenuesById[freetrainingDbo.venueId] ?: error("Venue not found for: $freetrainingDbo")
            freetrainingDbo.toFreetraining(venueForFreetraining)
        }.groupByTo(mutableMapOf()) { it.venue.id }
    }

    private val allVenues: MutableList<Venue> by lazy {
        val allVenueDbos = venueRepo.selectAll()
        // TODO implement venue linking (test first!)
//        val allVenuesById = allVenues.associateBy { it.id }
//        val allLinksById = venueLinksRepo.selectAll()
        allVenueDbos
            .map { venueDbo ->
                venueDbo.toVenue(baseUrl).also { venue ->
                    allActivitiesByVenueId[venue.id]?.also { activitiesForVenue ->
                        venue.activities += activitiesForVenue
                    }
                    allFreetrainingsByVenueId[venue.id]?.also { freetrainingForVenue ->
                        venue.freetrainings += freetrainingForVenue
                    }
                }
            }.toMutableList()
    }

    fun registerListener(listener: DataStorageListener) {
        listeners += listener
    }

    fun selectVisibleVenues(): List<Venue> =
        allVenues

    fun selectVenueById(id: Int) =
        allVenues.first { it.id == id }

    fun selectVisibleActivities(): List<Activity> =
        allActivitiesByVenueId.values.toList().flatten()

    fun selectVisibleFreetrainings(): List<Freetraining> =
        allFreetrainingsByVenueId.values.toList().flatten()

    override fun onVenueDboAdded(venueDbo: VenueDbo) {
        log.debug { "onVenueAdded($venueDbo)" }
        val venue = venueDbo.toVenue(baseUrl)
        allVenues += venue
        dispatchOnVenueAdded(venue)
    }

    override fun onActivityDbosAdded(activityDbos: List<ActivityDbo>) {
        val activities = activityDbos.map { activityDbo ->
            val venue = allVenues.firstOrNull { it.id == activityDbo.venueId }
                ?: error("Failed to add activity! Could not find venue by ID for: $activityDbo")
            val activity = activityDbo.toActivity(venue)
            // venue.activities += activity ... NO: done in SyncerViewModel (concurrency issues, IO vs Compose)
            allActivitiesByVenueId.getOrPut(activity.venue.id) { mutableListOf() }.add(activity)
            activity
        }
        dispatchOnActivitiesAdded(activities)
    }

    override fun onFreetrainingDbosAdded(freetrainingDbos: List<FreetrainingDbo>) {
        val freetrainings = freetrainingDbos.map { freetrainingDbo ->
            val venue = allVenues.firstOrNull { it.id == freetrainingDbo.venueId }
                ?: error("Failed to add freetraining! Could not find venue by ID for: $freetrainingDbos")
            val freetraining = freetrainingDbo.toFreetraining(venue)
            // venue.freetrainings += freetraining // NO! do it in SyncerViewModel
            allFreetrainingsByVenueId.getOrPut(freetraining.venue.id) { mutableListOf() }.add(freetraining)
            freetraining
        }
        dispatchOnFreetrainingsAdded(freetrainings)
    }

    override fun onFreetrainingDboUpdated(freetrainingDbo: FreetrainingDbo, field: FreetrainingFieldUpdate) {
        allFreetrainingsByVenueId.values.flatten().first { it.id == freetrainingDbo.id }.also { freetraining ->
            when (field) {
                FreetrainingFieldUpdate.WasCheckedin -> {
                    freetraining.wasCheckedin = freetrainingDbo.wasCheckedin
                }
            }
        }
    }

    override fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        val activity = allActivitiesByVenueId[activityDbo.venueId]!!.first { it.id == activityDbo.id }
        when (field) {
            ActivityFieldUpdate.IsBooked -> activity.isBooked = activityDbo.isBooked
            ActivityFieldUpdate.WasCheckedin -> activity.wasCheckedin = activityDbo.wasCheckedin
        }
    }

    fun update(venue: Venue) {
        log.debug { "updating $venue" }
        venueRepo.update(venue.toDbo())
        allActivitiesByVenueId[venue.id]?.forEach { activity ->
            activity.venue.updateSelfBy(venue)
        }
        allFreetrainingsByVenueId[venue.id]?.forEach { freetraining ->
            freetraining.venue.updateSelfBy(venue)
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

    private fun dispatchOnActivitiesAdded(activities: List<Activity>) {
        listeners.forEach {
            it.onActivitiesAdded(activities)
        }
    }

    private fun dispatchOnFreetrainingsAdded(freetrainings: List<Freetraining>) {
        listeners.forEach {
            it.onFreetrainingsAdded(freetrainings)
        }
    }
}

fun ActivityDbo.toActivity(venue: SimpleVenue) = Activity(
    id = id,
    venue = venue,
    name = name,
    category = category,
    spotsLeft = spotsLeft,
    teacher = teacher,
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
    wasCheckedin = wasCheckedin,
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
