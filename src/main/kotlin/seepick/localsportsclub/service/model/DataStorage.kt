package seepick.localsportsclub.service.model

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url
import seepick.localsportsclub.api.UscConfig
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.service.SinglesService
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.service.distance
import seepick.localsportsclub.service.round
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.FreetrainingFieldUpdate
import seepick.localsportsclub.sync.SyncerListener

interface DataStorageListener {
    fun onVenuesAdded(venues: List<Venue>)
    fun onVenueUpdated(venue: Venue)

    fun onActivitiesAdded(activities: List<Activity>)
    fun onActivitiesDeleted(activities: List<Activity>)

    fun onFreetrainingsAdded(freetrainings: List<Freetraining>)
    fun onFreetrainingsDeleted(freetrainings: List<Freetraining>)
}

object NoopDataStorageListener : DataStorageListener {
    override fun onVenuesAdded(venues: List<Venue>) {
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
    private val singlesService: SinglesService,
    uscConfig: UscConfig,
) : SyncerListener {

    private val log = logger {}
    private val baseUrl = uscConfig.baseUrl
    private val listeners = mutableListOf<DataStorageListener>()

    private val venuesById: MutableMap<Int, Venue> by lazy {
        singlesService.preferences.city?.id?.let { cityId ->
            val venues =
                venueRepo.selectAll(cityId).map { it.toVenue(baseUrl, singlesService.calculateLocatioAndDistance(it)) }
                    .associateBy { it.id }
            venueLinksRepo.selectAll(cityId).forEach { (id1, id2) ->
                val venue1 = venues[id1] ?: error("Linking venue1 not found by ID: $id1")
                val venue2 = venues[id2] ?: error("Linking venue2 not found by ID: $id2")
                venue1.linkedVenues += venue2
                venue2.linkedVenues += venue1
            }
            venues.toMutableMap()
        } ?: mutableMapOf()

    }

    val venuesCategories: List<String> by lazy {
        venuesById.values.asSequence().flatMap { it.categories }.distinct().filter { it.isNotEmpty() }.sorted().toList()
    }

    val activitiesCategories: List<String> by lazy {
        allActivitiesByVenueId.values.asSequence().flatten().map { it.category }.distinct().filter { it.isNotEmpty() }
            .sorted().toList()
    }

    val freetrainingsCategories: List<String> by lazy {
        allFreetrainingsByVenueId.values.asSequence().flatten().map { it.category }.distinct()
            .filter { it.isNotEmpty() }.sorted().toList()
    }

    private val allActivitiesByVenueId: MutableMap<Int, MutableList<Activity>> by lazy {
        singlesService.preferences.city?.id?.let { cityId ->
            val today = clock.today()
            activityRepo.selectAll(cityId).filter { it.state != ActivityState.Blank || it.from.toLocalDate() >= today }
                .sortedByDescending { it.from }.map { activityDbo ->
                    val venueForActivity = venuesById[activityDbo.venueId] ?: error("Venue not found for: $activityDbo")
                    activityDbo.toActivity(venueForActivity).also {
                        venueForActivity.activities += it
                    }
                }.groupByTo(mutableMapOf()) { it.venue.id }
        } ?: mutableMapOf()
    }

    private val allFreetrainingsByVenueId: MutableMap<Int, MutableList<Freetraining>> by lazy {
        singlesService.preferences.city?.id?.let { cityId ->
            val today = clock.today()
            freetrainingRepo.selectAll(cityId).filter { it.state != FreetrainingState.Blank || it.date >= today }
                .sortedByDescending { it.date }.map { freetrainingDbo ->
                    val venueForFreetraining =
                        venuesById[freetrainingDbo.venueId] ?: error("Venue not found for: $freetrainingDbo")
                    freetrainingDbo.toFreetraining(venueForFreetraining).also {
                        venueForFreetraining.freetrainings += it
                    }
                }.groupByTo(mutableMapOf()) { it.venue.id }
        } ?: mutableMapOf()
    }

    fun registerListener(listener: DataStorageListener) {
        log.debug { "Registering DataStorageListener: ${listener::class.qualifiedName}" }
        listeners += listener
    }

    fun selectVisibleVenues(): List<Venue> = venuesById.values.toList()

    fun selectVisibleActivities(): List<Activity> {
        val now = clock.now()
        return allActivitiesByVenueId.values.toList().flatten()
            // keep the past non-blanks one in the total list (for partner details table), but display only upcoming ones in big table
            .filter { it.dateTimeRange.from >= now }
    }

    fun selectVisibleFreetrainings(): List<Freetraining> {
        val today = clock.today()
        return allFreetrainingsByVenueId.values.toList().flatten().filter { it.date >= today }
    }

    override fun onVenueDbosAdded(venueDbos: List<VenueDbo>) {
        log.debug { "onVenueDbosAdded(venueDbos.size=${venueDbos.size})" }
        val venues = venueDbos.map { venueDbo ->
            val venue = venueDbo.toVenue(baseUrl, singlesService.calculateLocatioAndDistance(venueDbo))
            venuesById[venue.id] = venue
            venue
        }
        singlesService.preferences.city?.id?.also { cityId ->
            val links = venueLinksRepo.selectAll(cityId)
            venues.forEach { venue ->
                links.filter { (id1, id2) ->
                    id1 == venue.id || id2 == venue.id
                }.forEach { (id1, id2) ->
                    val venue1 = venuesById[id1] ?: error("Linking venue1 not found by ID: $id1")
                    val venue2 = venuesById[id2] ?: error("Linking venue2 not found by ID: $id2")
                    if (!venue1.linkedVenues.contains(venue2) && !venue2.linkedVenues.contains(venue1)) {
                        venue1.linkedVenues += venue2
                        venue2.linkedVenues += venue1
                    }
                }
            }
        }
        dispatchOnVenuesAdded(venues)
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

    override fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        allActivitiesByVenueId[activityDbo.venueId]?.singleOrNull { it.id == activityDbo.id }?.also { activity ->
            when (field) {
                ActivityFieldUpdate.State -> activity.state = activityDbo.state
                ActivityFieldUpdate.Teacher -> activity.teacher = activityDbo.teacher
            }
        } ?: log.warn {
            "Couldn't find activity in data storage. " + "Most likely trying to update something which is too old and not visible on the UI anyway. " + "Activity: $activityDbo"
        }
    }

    override fun onFreetrainingDboUpdated(freetrainingDbo: FreetrainingDbo, field: FreetrainingFieldUpdate) {
        allFreetrainingsByVenueId.values.flatten().firstOrNull { it.id == freetrainingDbo.id }?.also { freetraining ->
            when (field) {
                FreetrainingFieldUpdate.State -> freetraining.state = freetrainingDbo.state
            }
        } ?: log.warn {
            "Couldn't find freetraining in data storage. " + "Most likely trying to update something which is too old and not visible on the UI anyway. " + "Freetraining: $freetrainingDbo"
        }
    }

    override fun onActivityDbosDeleted(activityDbos: List<ActivityDbo>) {
        log.debug { "onActivityDbosDeleted(${activityDbos.joinToString { it.id.toString() }})" }
        val deletedActivities = activityDbos.mapNotNull { activityDbo ->
            val allActivitiesByVenue = allActivitiesByVenueId[activityDbo.venueId]
            allActivitiesByVenue?.singleOrNull { it.id == activityDbo.id }?.also {
                allActivitiesByVenue.remove(it)
            } // if null... deleted DBO which wasn't visible in the UI anyway
            // venuesById[activity.venue.id]!!.activities.remove(activity) // NO! do it in SyncerViewModel
        }
        dispatchOnActivitiesDeleted(deletedActivities)
    }

    override fun onFreetrainingDbosDeleted(freetrainingDbos: List<FreetrainingDbo>) {
        log.debug { "onFreetrainingDbosDeleted(${freetrainingDbos.joinToString { it.id.toString() }})" }
        val deletedFreetrainings = freetrainingDbos.mapNotNull { freetrainingDbo ->
            val allFreetrainingsByVenue = allFreetrainingsByVenueId[freetrainingDbo.venueId]
            allFreetrainingsByVenue?.singleOrNull { it.id == freetrainingDbo.id }?.also {
                allFreetrainingsByVenue.remove(it)
            } // if null... deleted DBO which wasn't visible in the UI anyway
            // venuesById[freetraining.venue.id]!!.freetrainings.remove(freetraining) // NO! do it in SyncerViewModel
        }
        dispatchOnFreetrainingsDeleted(deletedFreetrainings)
    }

    fun update(venue: Venue) {
        log.debug { "updating $venue" }
        venueRepo.update(venue.toDbo())
        dispatchOnVenueUpdated(venue)
    }

    private fun dispatchOnVenuesAdded(venues: List<Venue>) {
        listeners.forEach {
            it.onVenuesAdded(venues)
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

private fun SinglesService.calculateLocatioAndDistance(venueDbo: VenueDbo): Pair<Location?, Double?> {
    val location = if (venueDbo.latitude.isEmpty()) null else Location(
        latitude = venueDbo.latitude.toDouble(),
        longitude = venueDbo.longitude.toDouble(),
    )

    if (location == null) return null to null
    val home = preferences.home ?: return location to null
    return location to round(distance(home, location), 1)
}

fun ActivityDbo.toActivity(venue: Venue) = Activity(
    id = id,
    venue = venue,
    name = name,
    category = category,
    spotsLeft = spotsLeft,
    teacher = teacher,
    dateTimeRange = DateTimeRange(from, to),
    state = state,
)

fun FreetrainingDbo.toFreetraining(venue: Venue) = Freetraining(
    id = id,
    venue = venue,
    name = name,
    category = category,
    date = date,
    state = state,
)

fun Venue.toDbo() = VenueDbo(
    id = id,
    name = name,
    slug = slug,
    facilities = categories.joinToString(","),
    cityId = city.id,
    officialWebsite = officialWebsite,
    rating = rating.value,
    notes = notes,
    description = description,
    longitude = location?.longitude?.toString() ?: "",
    latitude = location?.latitude?.toString() ?: "",
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

fun VenueDbo.toVenue(
    baseUrl: Url,
    locationDistance: Pair<Location?, Double?>,
) = Venue(
    id = id,
    name = name,
    slug = slug,
    categories = facilities.split(",").filter { it.isNotEmpty() },
    city = City.byId(cityId),
    rating = Rating.byValue(rating),
    notes = notes,
    description = description,
    location = locationDistance.first,
    distanceInKm = locationDistance.second,
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
