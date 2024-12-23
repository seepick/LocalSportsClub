package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
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
import java.time.LocalDate

class ActivitiesSyncer(
    private val api: UscApi,
    private val city: City,
    private val plan: PlanType,
    private val syncDispatcher: SyncDispatcher,
    private val activityRepo: ActivityRepo,
    private val venueRepo: VenueRepo,
) {
    private val log = logger {}
    private val daysAhead = 14

    suspend fun sync() {
        log.info { "Syncing activities ..." }
        val allStoredActivities = activityRepo.selectAll()
        val venuesBySlug = venueRepo.selectAll().associateBy { it.slug }
        (0..<daysAhead).forEach { dayAhead ->
            val day = LocalDate.now().plusDays(dayAhead.toLong())
            syncForDay(day, allStoredActivities.filter { it.from.toLocalDate() == day }, venuesBySlug)
        }
        // FIXME delete old ones, before today, without a reservation on it
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
            syncDispatcher.dispatchActivityDboAdded(dbo)
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
