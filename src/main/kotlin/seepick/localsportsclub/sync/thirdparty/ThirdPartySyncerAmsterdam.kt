package seepick.localsportsclub.sync.thirdparty

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.SyncerListenerDispatcher
import java.time.LocalDate

class ThirdPartySyncerAmsterdam(
    private val activityRepo: ActivityRepo,
    private val venueRepo: VenueRepo,
    private val dispatcher: SyncerListenerDispatcher,
    private val clock: Clock,
    private val movementsYogaFetcher: MovementsYogaFetcher,
    private val deNieuweYogaSchoolFetcher: DeNieuweYogaSchoolFetcher,
    private val eversportsFetcher: EversportsFetcher,
) {
    private val log = logger {}

    suspend fun sync(days: List<LocalDate>) {
        log.info { "Syncing third party data..." }
        MovementsYogaStudio.entries.forEach {
            syncThirdParty(movementsYogaFetcher::fetch, it)
        }
        buildList {
            addAll(HotFlowYogaStudio.entries)
            add(
                EversportsFetchRequestImpl(
                    eversportsId = "J3pkIl",
                    slug = "movement-amsterdam",
                )
            )
        }.forEach { studio ->
            syncThirdParty(eversportsFetcher::fetch, studio)
        }
        syncThirdParty(deNieuweYogaSchoolFetcher::fetch, DeNieuweYogaSchoolFetcherRequest(days))
    }

    private suspend fun <P : HasSlug> syncThirdParty(fetch: suspend (P) -> List<ThirdEvent>, param: P) {
        log.debug { "Syncing third party events for venue: ${param.slug}" }
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

interface HasSlug {
    val slug: String
}

data class ThirdEvent(
    val title: String,
    val teacher: String,
    val dateTimeRange: DateTimeRange,
)
