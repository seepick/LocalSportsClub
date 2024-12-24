package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.annotations.TestOnly
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.activities.ActivitiesFilter
import seepick.localsportsclub.api.activities.ActivityInfo
import seepick.localsportsclub.api.activities.ServiceTye
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.Clock
import java.time.LocalDate

class ActivitiesSyncer(
    private val api: UscApi,
    private val activityRepo: ActivityRepo,
    private val venueRepo: VenueRepo,
    private val clock: Clock,
    private val dispatcher: SyncerListenerDispatcher,
    private val syncDaysAhead: Int = 14,
    uscConfig: UscConfig,
) {
    private val log = logger {}
    private val city: City = uscConfig.city
    private val plan: PlanType = uscConfig.plan

    init {
        require(syncDaysAhead >= 1)
    }

    suspend fun sync() {
        log.info { "Syncing activities ..." }
        val allStoredActivities = activityRepo.selectAll()
        val venuesBySlug = venueRepo.selectAll().associateBy { it.slug }

        daysToSync().also { log.debug { "Syncing days: $it" } }.forEach { day ->
            syncForDay(day, allStoredActivities.filter { it.from.toLocalDate() == day }, venuesBySlug)
        }
        // FIXME delete old ones, before today, without a reservation on it
    }

    @TestOnly
    fun daysToSync(): List<LocalDate> {
        val today = clock.today()
        val furthestAwaySyncDayIncl = today.plusDays(syncDaysAhead.toLong() - 1)
        val futureMostActivity = activityRepo.selectFutureMostDate() ?: today.minusDays(1)
        val startingPoint = if (futureMostActivity < today) today else futureMostActivity
        return startingPoint.datesUntil(furthestAwaySyncDayIncl.plusDays(1)).toList()
    }

    private suspend fun syncForDay(day: LocalDate, stored: List<ActivityDbo>, venuesBySlug: Map<String, VenueDbo>) {
        val remoteActivities = fetchRemoteActivities(day).associateBy { it.id }
        val storedActivities = stored.associateBy { it.id }

        val missingActivities = remoteActivities.minus(storedActivities.keys)
        log.debug { "For $day going to insert ${missingActivities.size} missing activities." }
        missingActivities.values.forEach { activity ->
            val venueId = venuesBySlug[activity.venueSlug]?.id ?: error("Unable to find venue by slug for: $activity")
            val dbo = activity.toDbo(venueId)
            activityRepo.insert(dbo)
            dispatcher.dispatchOnActivityDboAdded(dbo)
        }
    }

    private fun ActivityInfo.toDbo(venueId: Int) = ActivityDbo(
        id = id,
        venueId = venueId,
        name = name,
        category = category,
        spotsLeft = spotsLeft,
        from = from,
        to = to,
        scheduled = false,
    )

    private suspend fun fetchRemoteActivities(date: LocalDate) = api.fetchActivities(
        ActivitiesFilter(
            city = city,
            plan = plan,
            date = date,
            service = ServiceTye.Courses,
        )
    )
}
