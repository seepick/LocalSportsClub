package seepick.localsportsclub.service.model

import com.github.seepick.uscclient.model.City
import com.github.seepick.uscclient.plan.Plan
import com.github.seepick.uscclient.shared.DateTimeRange
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.CategoryService
import seepick.localsportsclub.service.GlobalRemarkFinder
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.service.RemarkService
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.distance
import seepick.localsportsclub.service.round
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.FreetrainingFieldUpdate
import seepick.localsportsclub.sync.SyncerListener
import seepick.localsportsclub.view.remark.RemarkViewEntity
import java.net.URL

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
    private val remarkService: RemarkService,
    private val globalRemarkFinder: GlobalRemarkFinder,
    private val categoryService: CategoryService,
    private val venueLinksRepo: VenueLinksRepo,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val clock: Clock,
    private val singlesService: SinglesService,
    private val baseUrl: URL,
) : SyncerListener {

    private val log = logger {}
    private val listeners = mutableListOf<DataStorageListener>()


    private val venuesById: MutableMap<Int, Venue> by lazy {
        singlesService.preferences.city?.id?.let { cityId ->
            val remarks = remarkService.selectAll()

            val venuesById = venueRepo.selectAllByCity(cityId).map { dbo ->
                dbo.toVenue(
                    baseUrl = baseUrl,
                    locationDistance = singlesService.calculateLocatioAndDistance(dbo),
                    categories = categoryService.findCategories(dbo),
                )
            }.associateBy { it.id }
            venuesById.forEach { (_, venue) ->
                venue.activityRemarks.addAll(remarks.forActivities(venue.id))
                venue.teacherRemarks.addAll(remarks.forTeachers(venue.id))
            }
            venueLinksRepo.selectAll(cityId).forEach { (id1, id2) ->
                val venue1 = venuesById[id1] ?: error("Linking venue1 not found by ID: $id1")
                val venue2 = venuesById[id2] ?: error("Linking venue2 not found by ID: $id2")
                venue1.linkedVenues += venue2
                venue2.linkedVenues += venue1
            }
            venuesById.toMutableMap()
        } ?: mutableMapOf()
    }

    val venueCategories: List<Category> by lazy {
        venuesById.values.asSequence().flatMap { it.categories }.distinct().filter { it.name.isNotEmpty() }.sorted()
            .toList()
    }

    val allCategories: List<Category> by lazy {
        (collectActivityCategories() + collectFreetrainingCategories() + allVenueCategories).distinct().sorted()
    }

    private val allVenueCategories: List<Category> by lazy {
        venuesById.values.map { it.categories }.flatten().distinct()
    }

    val availableActivityCategories: List<Category> by lazy {
        val now = clock.now()
        collectActivityCategories {
            // remove categories from past activities (checkedin)
            it.dateTimeRange.from >= now && !it.venue.isHidden
        }
    }

    private fun collectActivityCategories(filter: (Activity) -> Boolean = { true }): List<Category> =
        allActivitiesByVenueId.values.asSequence().flatten().filter { filter(it) }.map { it.category }.distinct()
            .filter { it.name.isNotEmpty() }.sorted().toList()

    val freetrainingsCategories: List<Category> by lazy {
        val today = clock.today()
        collectFreetrainingCategories {
            it.date >= today && !it.venue.isHidden
        }
    }

    private fun collectFreetrainingCategories(filter: (Freetraining) -> Boolean = { true }): List<Category> =
        allFreetrainingsByVenueId.values.asSequence().flatten().filter { filter(it) }.map { it.category }.distinct()
            .filter { it.name.isNotEmpty() }.sorted().toList()

    private val allActivitiesByVenueId: MutableMap<Int, MutableList<Activity>> by lazy {
        singlesService.preferences.city?.id?.let { cityId ->
            val today = clock.today()
            activityRepo.selectAll(cityId).filter { it.state != ActivityState.Blank || it.from.toLocalDate() >= today }
                .sortedByDescending { it.from }.map { activityDbo ->
                    val venueForActivity = venuesById[activityDbo.venueId] ?: error("Venue not found for: $activityDbo")

                    activityDbo.toActivity(
                        venue = venueForActivity,
                        category = categoryService.findCategoryByName(activityDbo.category),
                        globalActivityRemark = globalRemarkFinder.findForActivity(activityDbo.name),
                        globalTeacherRemark = globalRemarkFinder.findForTeacher(activityDbo.teacher),
                    )
                        .also {
                            venueForActivity.addActivities(setOf(it))
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
                    freetrainingDbo.toFreetraining(
                        venueForFreetraining, categoryService.findCategoryByName(freetrainingDbo.category)
                    ).also {
                        venueForFreetraining.addFreetrainings(setOf(it))
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

    override fun onVenueDbosAdded(addedVenues: List<VenueDbo>) {
        log.debug { "onVenueDbosAdded(venueDbos.size=${addedVenues.size})" }
        val venues = addedVenues.map { venueDbo ->
            val venue = venueDbo.toVenue(
                baseUrl = baseUrl,
                locationDistance = singlesService.calculateLocatioAndDistance(venueDbo),
                categories = categoryService.findCategories(venueDbo)
            )
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

    override fun onVenueDbosMarkedDeleted(deletedVenues: List<VenueDbo>) {
        deletedVenues.mapNotNull { venuesById[it.id] }.forEach { venue ->
            venue.isDeleted = true
        }
    }

    override fun onVenueDbosMarkedUndeleted(undeletedVenues: List<VenueDbo>) {
        undeletedVenues.mapNotNull { venuesById[it.id] }.forEach { venue ->
            venue.isDeleted = false
        }
    }

    override fun onActivityDbosAdded(addedActivities: List<ActivityDbo>) {
        val activities = addedActivities.map { activityDbo ->
            val venue = venuesById[activityDbo.venueId]
                ?: error("Failed to add activity! Could not find venue by ID for: $activityDbo")
            val activity = activityDbo.toActivity(
                venue = venue,
                category = categoryService.findCategoryByName(activityDbo.category),
                globalActivityRemark = globalRemarkFinder.findForActivity(activityDbo.name),
                globalTeacherRemark = globalRemarkFinder.findForTeacher(activityDbo.teacher),
            )
            // venue.activities += activity ... NO: done in SyncerViewModel (concurrency issues, IO vs Compose)
            allActivitiesByVenueId.getOrPut(activity.venue.id) { mutableListOf() }.add(activity)
            activity
        }
        dispatchOnActivitiesAdded(activities)
    }

    override fun onFreetrainingDbosAdded(addedFreetrainings: List<FreetrainingDbo>) {
        val freetrainings = addedFreetrainings.map { freetrainingDbo ->
            val venue = venuesById[freetrainingDbo.venueId]
                ?: error("Failed to add freetraining! Could not find venue by ID for: $addedFreetrainings")
            val freetraining =
                freetrainingDbo.toFreetraining(venue, categoryService.findCategoryByName(freetrainingDbo.category))
            // venue.freetrainings += freetraining // NO! do it in SyncerViewModel
            allFreetrainingsByVenueId.getOrPut(freetraining.venue.id) { mutableListOf() }.add(freetraining)
            freetraining
        }
        dispatchOnFreetrainingsAdded(freetrainings)
    }

    override fun onActivityDboUpdated(updatedActivity: ActivityDbo, field: ActivityFieldUpdate) {
        allActivitiesByVenueId[updatedActivity.venueId]?.singleOrNull { it.id == updatedActivity.id }
            ?.also { activity ->
                when (field) {
                    is ActivityFieldUpdate.State -> activity.state = updatedActivity.state
                    ActivityFieldUpdate.Teacher -> activity.teacher = updatedActivity.teacher
                    ActivityFieldUpdate.Description -> activity.description = updatedActivity.description
                    ActivityFieldUpdate.SpotsLeft -> activity.spotsLeft = updatedActivity.spotsLeft
                    ActivityFieldUpdate.CancellationLimit -> activity.cancellationLimit =
                        updatedActivity.cancellationLimit
                }
            } ?: log.warn {
            "Couldn't find activity in data storage. " + "Most likely trying to update something which is too old and not visible on the UI anyway. " + "Activity: $updatedActivity"
        }
    }

    override fun onFreetrainingDboUpdated(updatedFreetraining: FreetrainingDbo, field: FreetrainingFieldUpdate) {
        allFreetrainingsByVenueId.values.flatten().firstOrNull { it.id == updatedFreetraining.id }
            ?.also { freetraining ->
                when (field) {
                    FreetrainingFieldUpdate.State -> freetraining.state = updatedFreetraining.state
                }
            } ?: log.warn {
            "Couldn't find freetraining in data storage. " + "Most likely trying to update something which is too old and not visible on the UI anyway. " + "Freetraining: $updatedFreetraining"
        }
    }

    override fun onActivityDbosDeleted(deletedActivities: List<ActivityDbo>) {
        log.debug { "onActivityDbosDeleted(${deletedActivities.joinToString { it.id.toString() }})" }
        val effectivelyDeleted = deletedActivities.mapNotNull { activityDbo ->
            val allActivitiesByVenue = allActivitiesByVenueId[activityDbo.venueId]
            allActivitiesByVenue?.singleOrNull { it.id == activityDbo.id }?.also {
                allActivitiesByVenue.remove(it)
            } // if null... deleted DBO which wasn't visible in the UI anyway
            // venuesById[activity.venue.id]!!.activities.remove(activity) // NO! do it in SyncerViewModel
        }
        dispatchOnActivitiesDeleted(effectivelyDeleted)
    }

    override fun onFreetrainingDbosDeleted(deletedFreetrainings: List<FreetrainingDbo>) {
        log.debug { "onFreetrainingDbosDeleted(${deletedFreetrainings.joinToString { it.id.toString() }})" }
        val effectivelyDeleted = deletedFreetrainings.mapNotNull { freetrainingDbo ->
            val allFreetrainingsByVenue = allFreetrainingsByVenueId[freetrainingDbo.venueId]
            allFreetrainingsByVenue?.singleOrNull { it.id == freetrainingDbo.id }?.also {
                allFreetrainingsByVenue.remove(it)
            } // if null... deleted DBO which wasn't visible in the UI anyway
            // venuesById[freetraining.venue.id]!!.freetrainings.remove(freetraining) // NO! do it in SyncerViewModel
        }
        dispatchOnFreetrainingsDeleted(effectivelyDeleted)
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

    private fun CategoryService.findCategories(dbo: VenueDbo): List<Category> =
        dbo.facilities.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .map { Category(name = it, rating = findCategoryByName(it).rating) }

}

private fun SinglesService.calculateLocatioAndDistance(venueDbo: VenueDbo): Pair<Location, Double> {
    val location = Location(
        latitude = venueDbo.latitude.toDouble(),
        longitude = venueDbo.longitude.toDouble(),
    )
    val home = preferences.home ?: return location to 0.0
    return location to round(distance(home, location), 1)
}

fun ActivityDbo.toActivity(
    venue: Venue,
    category: Category,
    globalActivityRemark: RemarkViewEntity?,
    globalTeacherRemark: RemarkViewEntity?,
) = Activity(
    id = id,
    venue = venue,
    name = name,
    category = category,
    spotsLeft = spotsLeft,
    teacher = teacher,
    description = description,
    dateTimeRange = DateTimeRange(from, to),
    state = state,
    plan = Plan.UscPlan.byId(planId),
    cancellationLimit = cancellationLimit,
    globalActivityRemark = globalActivityRemark,
    globalTeacherRemark = globalTeacherRemark,
)

fun FreetrainingDbo.toFreetraining(venue: Venue, category: Category) = Freetraining(
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
    facilities = categories.joinToString(",") { it.name },
    cityId = city.id,
    officialWebsite = officialWebsite,
    rating = rating.value,
    notes = notes,
    description = description,
    longitude = location.longitude.toString(),
    latitude = location.latitude.toString(),
    street = street,
    addressLocality = addressLocality,
    postalCode = postalCode,
    importantInfo = importantInfo,
    openingTimes = openingTimes,
    imageFileName = imageFileName,
    isFavorited = isFavorited,
    isWishlisted = isWishlisted,
    isHidden = isHidden,
    isAutoSync = isAutoSync,
    isDeleted = isDeleted,
    planId = plan.id,
    visitLimits = visitLimits,
    lastSync = lastSync,
)

fun VenueDbo.toVenue(
    baseUrl: URL,
    locationDistance: Pair<Location, Double>,
    categories: List<Category>,
) = Venue(
    id = id,
    name = name,
    slug = slug,
    categories = categories,
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
    uscWebsite = "${baseUrl.toString().trimEnd('/')}/venues/$slug",
    isFavorited = isFavorited,
    isWishlisted = isWishlisted,
    isHidden = isHidden,
    imageFileName = imageFileName,
    isDeleted = isDeleted,
    isAutoSync = isAutoSync,
    plan = Plan.UscPlan.byId(planId),
    visitLimits = visitLimits,
    lastSync = lastSync,
)
