package seepick.localsportsclub.service

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.activity.ActivityDetails
import com.github.seepick.uscclient.shared.DateRange
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.sync.SyncProgress

class DnysActivityDetailsFetcher(
    private val venueRepo: VenueRepo,
    private val api: UscApi,
    private val progress: SyncProgress,
) {
    private val log = logger {}

    private val slug = "de-nieuwe-yogaschool"

    suspend fun enrich(original: List<Pair<ActivityDbo, ActivityDetails>>): List<Pair<ActivityDbo, ActivityDetails>> {
        val venue = venueRepo.selectBySlug(slug) ?: return original
        val dnysActivities = original.filter {
            it.first.venueId == venue.id && it.first.teacher == null
        }
        if (dnysActivities.isEmpty()) return original
        log.info { "Enriching DNYS activity details." }
        progress.onProgress("DNYS")
        val dates = dnysActivities.map { it.first.from }.sorted()
        val dateRange = DateRange(dates.first().toLocalDate(), dates.last().toLocalDate())
        val dnysEvents = api.fetchDnysEvents(dateRange)
        val dnysOverrides = mutableMapOf<ActivityDbo, ActivityDetails>()
        dnysEvents.forEach { event ->
            val matchingActivity = dnysActivities.firstOrNull { orig ->
                // manually adjust UTC to amsterdam+1
                orig.first.from == event.dateTimeRange.from.plusHours(1) &&
                        orig.first.to == event.dateTimeRange.to.plusHours(1)
                // check title too??
            } ?: return@forEach
            dnysOverrides += matchingActivity.first to matchingActivity.second.copy(teacher = event.teacher)
        }
        val result = original.toMutableList()
        result.removeAll { dnysOverrides.containsKey(it.first) }
        result += dnysOverrides.toList()
        return result
    }
}
