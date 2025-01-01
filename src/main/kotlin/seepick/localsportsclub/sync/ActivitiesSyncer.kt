package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.activity.ActivitiesFilter
import seepick.localsportsclub.api.activity.ActivityInfo
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.date.Clock
import java.time.LocalDate

class ActivitiesSyncer(
    private val clock: Clock,
    private val api: UscApi,
    private val activityRepo: ActivityRepo,
    private val venueRepo: VenueRepo,
    private val venueSyncInserter: VenueSyncInserter,
    private val dispatcher: SyncerListenerDispatcher,
    uscConfig: UscConfig,
) {
    private val log = logger {}
    private val city: City = uscConfig.city
    private val plan: PlanType = uscConfig.plan
    private val syncDaysAhead: Int = uscConfig.syncDaysAhead

    suspend fun sync() {
        log.info { "Syncing activities ..." }
        val allStoredActivities = activityRepo.selectAll()
        val venuesBySlug = venueRepo.selectAll().associateBy { it.slug }.toMutableMap()

        clock.daysUntil(syncDaysAhead, activityRepo.selectFutureMostDate())
            .also { log.debug { "Syncing days: $it" } }
            .forEach { day ->
                syncForDay(day, allStoredActivities.filter { it.from.toLocalDate() == day }, venuesBySlug)
            }
    }

    private suspend fun syncForDay(
        day: LocalDate,
        stored: List<ActivityDbo>,
        venuesBySlug: MutableMap<String, VenueDbo>
    ) {
        val remoteActivities =
            api.fetchActivities(ActivitiesFilter(city = city, plan = plan, date = day)).associateBy { it.id }
        val storedActivities = stored.associateBy { it.id }

        val missingActivities = remoteActivities.minus(storedActivities.keys)
        log.debug { "For $day going to insert ${missingActivities.size} missing activities." }
        val dbos = missingActivities.values.map { activity ->
            syncMissingActivity(activity, venuesBySlug)
        }
        dispatcher.dispatchOnActivityDbosAdded(dbos)
    }

    private suspend fun syncMissingActivity(
        activity: ActivityInfo,
        venuesBySlug: MutableMap<String, VenueDbo>
    ): ActivityDbo {
        val venueId = venuesBySlug[activity.venueSlug]?.id ?: rescueVenue(activity, venuesBySlug)
        val dbo = activity.toDbo(venueId)
        activityRepo.insert(dbo)
        return dbo
    }

    private suspend fun rescueVenue(activity: ActivityInfo, venuesBySlug: MutableMap<String, VenueDbo>): Int {
        log.debug { "Trying to rescue venue for missing: $activity" }
        venueSyncInserter.fetchAllInsertDispatch(
            listOf(activity.venueSlug),
            "[SYNC] fetched through activity ${activity.name}"
        )
        return venueRepo.selectBySlug(activity.venueSlug)?.also {
            venuesBySlug[it.slug] = it
        }?.id ?: error("Unable to find venue by slug for: $activity")
    }

    private fun ActivityInfo.toDbo(venueId: Int) = ActivityDbo(
        id = id,
        venueId = venueId,
        name = name,
        category = category,
        spotsLeft = spotsLeft,
        from = dateTimeRange.from,
        to = dateTimeRange.to,
        teacher = null,
        isBooked = false,
        wasCheckedin = false,
    )
}
