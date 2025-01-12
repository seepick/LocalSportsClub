package seepick.localsportsclub.sync.thirdparty

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.SyncerListenerDispatcher

class ThirdPartySyncer(
    private val activityRepo: ActivityRepo,
    private val venueRepo: VenueRepo,
    private val dispatcher: SyncerListenerDispatcher,
    private val clock: Clock,
    
    private val movementsYogaFetcher: MovementsYogaFetcher,
    private val hotFlowYogaFetcher: HotFlowYogaFetcher,
    // movement amsterdam
    // de nieuwe yoga school
) {
    private val log = logger {}
    suspend fun sync() {
        log.info { "Syncing third party data..." }
        MovementsYogaVenue.entries.forEach {
            syncThirdParty(movementsYogaFetcher::fetch, it)
        }
        HotFlowYogaVenue.entries.forEach {
            syncThirdParty(hotFlowYogaFetcher::fetch, it)
        }
    }

    private suspend fun <P : ThirdVenue> syncThirdParty(fetch: suspend (P) -> List<ThirdEvent>, param: P) {
        log.debug { "Syncing events for venue: ${param.slug}" }
        val venue = venueRepo.selectBySlug(param.slug)
            ?: error("Venue not found in DB with slug [${param.slug}]")
        val events = fetch(param)
        val now = clock.now()
        activityRepo.selectAllForVenueId(venue.id).filter {
            it.teacher == null && it.from >= now
        }.forEach { activity ->
            events.firstOrNull {
                it.dateTimeRange.from == activity.from && it.dateTimeRange.to == activity.to
            }?.also { event ->
                log.trace { "Adding teacher [${event.teacher}] for $activity" }
                val updated = activity.copy(teacher = event.teacher)
                activityRepo.update(updated)
                dispatcher.dispatchOnActivityDboUpdated(updated, ActivityFieldUpdate.Teacher)
            }
        }
    }
}

interface ThirdVenue {
    val slug: String
}

data class ThirdEvent(
    val title: String,
    val dateTimeRange: DateTimeRange,
    val teacher: String,
)
