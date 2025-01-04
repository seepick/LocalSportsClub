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
    fun onActivitiesDeleted(activities: List<Activity>)
    fun onFreetrainingsDeleted(freetrainings: List<Freetraining>)
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

    override fun onActivitiesDeleted(activities: List<Activity>) {
    }

    override fun onFreetrainingsDeleted(freetrainings: List<Freetraining>) {
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

    private val venuesById: MutableMap<Int, Venue> by lazy {
        venueRepo.selectAll().map { it.toVenue(baseUrl) }.associateBy { it.id }.toMutableMap()
    }

    private val allActivitiesByVenueId: MutableMap<Int, MutableList<Activity>> by lazy {
        val today = clock.today()
        activityRepo.selectAll()
            .filter { it.wasCheckedin || it.from.toLocalDate() >= today }
            .sortedByDescending { it.from }
            .map { activityDbo ->
                val venueForActivity = venuesById[activityDbo.venueId] ?: error("Venue not found for: $activityDbo")
                activityDbo.toActivity(venueForActivity).also {
                    venueForActivity.activities += it
                }
            }.groupByTo(mutableMapOf()) { it.venue.id }
    }

    private val allFreetrainingsByVenueId: MutableMap<Int, MutableList<Freetraining>> by lazy {
        val today = clock.today()
        freetrainingRepo.selectAll()
            .filter { it.wasCheckedin || it.date >= today }
            .sortedByDescending { it.date }
            .map { freetrainingDbo ->
                val venueForFreetraining =
                    venuesById[freetrainingDbo.venueId] ?: error("Venue not found for: $freetrainingDbo")
                freetrainingDbo.toFreetraining(venueForFreetraining).also {
                    venueForFreetraining.freetrainings += it
                }
            }.groupByTo(mutableMapOf()) { it.venue.id }
    }

    fun registerListener(listener: DataStorageListener) {
        listeners += listener
    }

    fun selectVisibleVenues(): List<Venue> =
        venuesById.values.toList()

    fun selectVisibleActivities(): List<Activity> {
        val today = clock.today()
        return allActivitiesByVenueId.values.toList().flatten().filter { it.dateTimeRange.from.toLocalDate() >= today }
    }

    fun selectVisibleFreetrainings(): List<Freetraining> {
        val today = clock.today()
        return allFreetrainingsByVenueId.values.toList().flatten().filter { it.date >= today }
    }

    override fun onVenueDboAdded(venueDbo: VenueDbo) {
        log.debug { "onVenueAdded($venueDbo)" }
        val venue = venueDbo.toVenue(baseUrl)
        venuesById[venue.id] = venue
        dispatchOnVenueAdded(venue)
    }

    override fun onActivityDbosAdded(activityDbos: List<ActivityDbo>) {
        val activities = activityDbos.map { activityDbo ->
            val venue = venuesById[activityDbo.venueId]
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
            val venue = venuesById[freetrainingDbo.venueId]
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

    override fun onActivityDbosDeleted(activityDbos: List<ActivityDbo>) {
        val deletedActivities = activityDbos.map { activityDbo ->
            val allActivitiesByVenue = allActivitiesByVenueId[activityDbo.venueId]!!
            val activity = allActivitiesByVenue.single { it.id == activityDbo.id }
            allActivitiesByVenue.remove(activity)
            // venuesById[activity.venue.id]!!.activities.remove(activity) // NO! do it in SyncerViewModel
            activity
        }
        dispatchOnActivitiesDeleted(deletedActivities)
    }

    override fun onFreetrainingDbosDeleted(freetrainingDbos: List<FreetrainingDbo>) {
        val deletedFreetrainings = freetrainingDbos.map { freetrainingDbo ->
            val allFreetrainingsByVenue = allFreetrainingsByVenueId[freetrainingDbo.venueId]!!
            val freetraining = allFreetrainingsByVenue.single { it.id == freetrainingDbo.id }
            allFreetrainingsByVenue.remove(freetraining)
            // venuesById[freetraining.venue.id]!!.freetrainings.remove(freetraining) // NO! do it in SyncerViewModel
            freetraining
        }
        dispatchOnFreetrainingsDeleted(deletedFreetrainings)
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

    private fun dispatchOnActivitiesDeleted(activities: List<Activity>) {
        listeners.forEach {
            it.onActivitiesDeleted(activities)
        }
    }

    private fun dispatchOnFreetrainingsDeleted(freetrainings: List<Freetraining>) {
        listeners.forEach {
            it.onFreetrainingsDeleted(freetrainings)
        }
    }
}

fun ActivityDbo.toActivity(venue: Venue) = Activity(
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

fun FreetrainingDbo.toFreetraining(venue: Venue) = Freetraining(
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
