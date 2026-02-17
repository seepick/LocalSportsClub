package seepick.localsportsclub.sync

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.activity.ActivitiesFilter
import com.github.seepick.uscclient.activity.ActivityInfo
import com.github.seepick.uscclient.model.City
import com.github.seepick.uscclient.plan.Plan
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.model.ActivityState
import java.time.LocalDate

fun SyncProgress.onProgressActivities(detail: String?) {
    onProgress("Activities", detail)
}

class ActivitiesSyncer(
    private val api: UscApi,
    private val activityRepo: ActivityRepo,
    private val venueRepo: VenueRepo,
    private val venueSyncInserter: VenueSyncInserter,
    private val dispatcher: SyncerListenerDispatcher,
    private val progress: SyncProgress,
) {
    private val log = logger {}

    suspend fun sync(
        plan: com.github.seepick.uscclient.plan.Plan,
        city: com.github.seepick.uscclient.model.City,
        days: List<LocalDate>,
    ) {
        log.info { "Syncing activities for: $days" }
        val allStoredActivities = activityRepo.selectAll(city.id)
        val venuesBySlug = venueRepo.selectAllByCity(city.id).associateBy { it.slug }.toMutableMap()
        days.forEachIndexed { index, day ->
            progress.onProgressActivities("Day ${index + 1}/${days.size}")
            syncForDay(
                plan,
                city,
                day,
                allStoredActivities,
                // filter { it.from.toLocalDate() == day } ...
                // NO, simply pass ALL of them (duplicate IDs could be _before_ sync range)
                venuesBySlug,
            )
        }
    }

    private suspend fun syncForDay(
        plan: Plan,
        city: City,
        day: LocalDate,
        stored: List<ActivityDbo>,
        venuesBySlug: MutableMap<String, VenueDbo>,
    ) {
        log.debug { "Sync for day: $day" }
        val remoteActivities =
            api.fetchActivities(ActivitiesFilter(city = city, plan = plan, date = day)).associateBy { it.id }
        val storedActivities = stored.associateBy { it.id }

        remoteActivities.filterKeys { storedActivities.containsKey(it) }.forEach {
            log.warn { "IGNORE: Duplicate remote activity by ID [${it.key}] found (already locally stored in DB): ${it.value}" }
        }
        val missingActivities = remoteActivities.minus(storedActivities.keys)
        log.debug { "For $day going to insert ${missingActivities.size} missing activities." }
        val dbos = missingActivities.values.map { activity ->
            syncMissingActivity(city, activity, venuesBySlug)
        }
        dispatcher.dispatchOnActivityDbosAdded(dbos)
    }

    private suspend fun syncMissingActivity(
        city: City,
        activity: ActivityInfo,
        venuesBySlug: MutableMap<String, VenueDbo>,
    ): ActivityDbo {
        val venueId = venuesBySlug[activity.venueSlug]?.id ?: rescueVenue(city, activity, venuesBySlug)
        val dbo = activity.toDbo(venueId)
        activityRepo.insert(dbo)
        return dbo
    }

    private suspend fun rescueVenue(
        city: City,
        activity: ActivityInfo,
        venuesBySlug: MutableMap<String, VenueDbo>,
    ): Int {
        log.debug { "Trying to rescue venue for missing: $activity" }
        venueSyncInserter.fetchInsertAndDispatch(
            city, listOf(VenueMeta(slug = activity.venueSlug, plan = null)),
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
        state = ActivityState.Blank,
        cancellationLimit = null,
        planId = plan.id,
        teacher = null,
        description = null,
    )

}
