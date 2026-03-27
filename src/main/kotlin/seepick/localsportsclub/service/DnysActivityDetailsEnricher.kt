package seepick.localsportsclub.service

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.activity.ActivityDetails
import com.github.seepick.uscclient.shared.DateRange
import com.github.seepick.uscclient.thirdparty.DnysEvent
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.sync.SyncProgress

class DnysActivityDetailsEnricher(
    private val venueRepo: VenueRepo,
    private val api: UscApi,
    private val progress: SyncProgress,
) : ActivityDetailsEnricher {
    private val log = logger {}

    private val slug = "de-nieuwe-yogaschool"

    override suspend fun enrich(original: ActivityDbosWithDetails): ActivityDbosWithDetails {
        val dnysActivities = filterDnysActivities(original) ?: return original
        log.info { "Enriching DNYS activity details for ${dnysActivities.size} DNYS events without teacher." }
        progress.onProgress("DNYS")
        val dnysEvents = api.fetchDnysEventsFor(dnysActivities)
        val enrichedActivities = enrichMatchingActivitiesByEvent(dnysEvents, dnysActivities)
        return original.overrideWith(enrichedActivities)
    }

    private fun enrichMatchingActivitiesByEvent(
        dnysEvents: List<DnysEvent>,
        dnysActivities: ActivityDbosWithDetails,
    ): ActivityDbosWithDetails =
        dnysEvents.mapNotNull { event ->
            val matchingActivities = dnysActivities.filter { orig ->
                orig.key.from == event.dateTimeRange.from.plusHours(2) &&
                        orig.key.to == event.dateTimeRange.to.plusHours(2)
            }
            when (matchingActivities.size) {
                0 -> null
                1 -> {
                    val matching = matchingActivities.iterator().next()
                    matching.key to matching.value.copy(teacher = event.teacher)
                }

                else -> error("Multiple matching DNYS found: $matchingActivities")
            }
        }.toMap()

    private fun ActivityDbosWithDetails.overrideWith(
        enrichedActivities: Map<ActivityDbo, ActivityDetails>,
    ): ActivityDbosWithDetails {
        log.debug { "Found ${enrichedActivities.size} DNYS event overrides." }
        val result = this.toMutableMap()
        enrichedActivities.forEach { (dbo, details) ->
            result[dbo] = details
        }
        return result
    }

    private fun filterDnysActivities(original: ActivityDbosWithDetails): ActivityDbosWithDetails? {
        val venue = venueRepo.selectBySlug(slug) ?: return null
        val dnysActivities = original.filter {
            it.key.venueId == venue.id && it.value.teacher == null
        }
        if (dnysActivities.isEmpty()) return null
        return dnysActivities
    }

    private suspend fun UscApi.fetchDnysEventsFor(dnysActivities: ActivityDbosWithDetails): List<DnysEvent> {
        val dates = dnysActivities.map { it.key.from }.sorted()
        val dateRange = DateRange(dates.first().toLocalDate(), dates.last().toLocalDate())
        return fetchDnysEvents(dateRange)
    }
}
