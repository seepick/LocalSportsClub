package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.activity.ActivitiesFilter
import seepick.localsportsclub.api.activity.FreetrainingInfo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.date.Clock
import java.time.LocalDate

class FreetrainingSyncer(
    private val clock: Clock,
    private val api: UscApi,
    private val freetrainingRepo: FreetrainingRepo,
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
        log.info { "Syncing freetrainiings ..." }
        val allStoredFreetrainings = freetrainingRepo.selectAll()
        val venuesBySlug = venueRepo.selectAll().associateBy { it.slug }.toMutableMap()

        clock.daysUntil(syncDaysAhead, freetrainingRepo.selectFutureMostDate())
            .also { log.debug { "Syncing days: $it" } }
            .forEach { day ->
                syncForDay(day, allStoredFreetrainings.filter { it.date == day }, venuesBySlug)
            }
    }

    private suspend fun syncForDay(
        day: LocalDate,
        stored: List<FreetrainingDbo>,
        venuesBySlug: MutableMap<String, VenueDbo>
    ) {
        val remoteFreetrainings =
            api.fetchFreetrainings(ActivitiesFilter(city = city, plan = plan, date = day)).associateBy { it.id }
        val storedFreetrainings = stored.associateBy { it.id }

        val missingFreetrainings = remoteFreetrainings.minus(storedFreetrainings.keys)
        log.debug { "For $day going to insert ${missingFreetrainings.size} missing freetrainings." }
        missingFreetrainings.values.forEach { freetraining ->
            val venueId = venuesBySlug[freetraining.venueSlug]?.id ?: suspend {
                log.debug { "Trying to rescue venue for missing: $freetraining" }
                venueSyncInserter.fetchAllInsertDispatch(
                    listOf(freetraining.venueSlug),
                    "[SYNC] fetched through freetraining ${freetraining.name}"
                )
                venueRepo.selectBySlug(freetraining.venueSlug)?.also {
                    venuesBySlug[it.slug] = it
                }?.id ?: error("Unable to find venue by slug for: $freetraining")
            }()
            val dbo = freetraining.toDbo(venueId, day)
            freetrainingRepo.insert(dbo)
            dispatcher.dispatchOnFreetrainingDboAdded(dbo)
        }
    }
}

private fun FreetrainingInfo.toDbo(venueId: Int, date: LocalDate) = FreetrainingDbo(
    id = id,
    venueId = venueId,
    name = name,
    category = category,
    date = date,
    wasCheckedin = false,
)
