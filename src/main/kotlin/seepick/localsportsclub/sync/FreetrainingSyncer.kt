package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.PhpSessionId
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.activity.ActivitiesFilter
import seepick.localsportsclub.api.activity.FreetrainingInfo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.Plan
import java.time.LocalDate

fun SyncProgress.onProgressFreetrainings(detail: String?) {
    onProgress("Freetrainings", detail)
}

class FreetrainingSyncer(
    private val api: UscApi,
    private val freetrainingRepo: FreetrainingRepo,
    private val venueRepo: VenueRepo,
    private val venueSyncInserter: VenueSyncInserter,
    private val dispatcher: SyncerListenerDispatcher,
    private val progress: SyncProgress,
) {
    private val log = logger {}

    suspend fun sync(session: PhpSessionId, plan: Plan, city: City, days: List<LocalDate>) {
        log.info { "Syncing freetrainiings for: $days" }
        val allStoredFreetrainings = freetrainingRepo.selectAll(city.id)
        val venuesBySlug = venueRepo.selectAll(city.id).associateBy { it.slug }.toMutableMap()
        days.forEachIndexed { index, day ->
            progress.onProgressFreetrainings("Day ${index + 1}/${days.size}")
            syncForDay(session, plan, city, day, allStoredFreetrainings.filter { it.date == day }, venuesBySlug)
        }
    }

    private suspend fun syncForDay(
        session: PhpSessionId,
        plan: Plan,
        city: City,
        day: LocalDate,
        stored: List<FreetrainingDbo>,
        venuesBySlug: MutableMap<String, VenueDbo>
    ) {
        log.debug { "Sync for day: $day" }
        val remoteFreetrainings =
            api.fetchFreetrainings(session, ActivitiesFilter(city = city, plan = plan, date = day))
                .associateBy { it.id }
        val storedFreetrainings = stored.associateBy { it.id }

        val missingFreetrainings = remoteFreetrainings.minus(storedFreetrainings.keys)
        log.debug { "For $day going to insert ${missingFreetrainings.size} missing freetrainings." }
        val dbos = missingFreetrainings.values.map { freetraining ->
            val venueId = venuesBySlug[freetraining.venueSlug]?.id ?: suspend {
                log.debug { "Trying to rescue venue for missing: $freetraining" }
                venueSyncInserter.fetchInsertAndDispatch(
                    session,
                    city,
                    listOf(freetraining.venueSlug),
                    "[SYNC] fetched through freetraining ${freetraining.name}"
                )
                venueRepo.selectBySlug(freetraining.venueSlug)?.also {
                    venuesBySlug[it.slug] = it
                }?.id ?: error("Unable to find venue by slug for: $freetraining")
            }()
            val dbo = freetraining.toDbo(venueId, day)
            freetrainingRepo.insert(dbo)
            dbo
        }
        dispatcher.dispatchOnFreetrainingDbosAdded(dbos)
    }
}

private fun FreetrainingInfo.toDbo(venueId: Int, date: LocalDate) = FreetrainingDbo(
    id = id,
    venueId = venueId,
    name = name,
    category = category,
    date = date,
    state = FreetrainingState.Blank,
)
