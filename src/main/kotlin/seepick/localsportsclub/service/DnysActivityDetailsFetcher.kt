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
) : ActivityEnricher {
    private val log = logger {}

    private val slug = "de-nieuwe-yogaschool"

    override suspend fun enrich(original: ActivityDbosWithDetails): ActivityDbosWithDetails {
        val venue = venueRepo.selectBySlug(slug) ?: return original
        val dnysActivities = original.filter {
            it.first.venueId == venue.id && it.first.teacher == null
        }
        if (dnysActivities.isEmpty()) return original
        log.info { "Enriching DNYS activity details for ${dnysActivities.size} DNYS events without teacher." }
        progress.onProgress("DNYS")
        val dates = dnysActivities.map { it.first.from }.sorted()
        val dateRange = DateRange(dates.first().toLocalDate(), dates.last().toLocalDate())
        val dnysEvents = api.fetchDnysEvents(dateRange)
        val dnysOverrides = mutableMapOf<ActivityDbo, ActivityDetails>()
        dnysEvents.forEach { event ->
            val matchingActivity = dnysActivities.firstOrNull { orig ->
                orig.first.from == event.dateTimeRange.from.plusHours(2) &&
                        orig.first.to == event.dateTimeRange.to.plusHours(2)
            } ?: return@forEach
            dnysOverrides += matchingActivity.first to matchingActivity.second.copy(teacher = event.teacher)
        }
        log.debug { "Found ${dnysOverrides.size} DNYS event overrides." }
        val result = original.toMutableList()
        result.removeAll { dnysOverrides.containsKey(it.first) }
        result += dnysOverrides.toList()
        return result
    }
}
