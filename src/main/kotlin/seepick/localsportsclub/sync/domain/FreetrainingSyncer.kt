package seepick.localsportsclub.sync.domain

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.activity.ActivitiesFilter
import com.github.seepick.uscclient.activity.FreetrainingInfo
import com.github.seepick.uscclient.model.City
import com.github.seepick.uscclient.plan.Plan
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.sync.SyncProgress
import seepick.localsportsclub.sync.SyncerListenerDispatcher
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

    suspend fun sync(
        plan: Plan,
        city: City,
        days: List<LocalDate>,
    ) {
        log.info { "Syncing freetrainiings for: $days" }
        val allStoredFreetrainings = freetrainingRepo.selectAll(city.id)
        val venuesBySlug = venueRepo.selectAllByCity(city.id).associateBy { it.slug }.toMutableMap()
        days.forEachIndexed { index, day ->
            progress.onProgressFreetrainings("Day ${index + 1}/${days.size}")
            syncForDay(plan, city, day, allStoredFreetrainings.filter { it.date == day }, venuesBySlug)
        }
    }

    private suspend fun syncForDay(
        plan: Plan,
        city: City,
        day: LocalDate,
        stored: List<FreetrainingDbo>,
        venuesBySlug: MutableMap<String, VenueDbo>,
    ) {
        log.debug { "Sync for day: $day" }
        val remoteFreetrainings =
            api.fetchFreetrainings(ActivitiesFilter(city = city, plan = plan, date = day))
                .associateBy { it.id }
        val storedFreetrainings = stored.associateBy { it.id }

        val missingFreetrainings = remoteFreetrainings.minus(storedFreetrainings.keys)
        log.debug { "For $day going to insert ${missingFreetrainings.size} missing freetrainings." }
        val dbos = missingFreetrainings.values.map { freetraining ->
            val venueId = venuesBySlug[freetraining.venueSlug]?.id ?: suspend {
                log.debug { "Trying to rescue venue for missing: $freetraining" }
                venueSyncInserter.fetchInsertAndDispatch(
                    city, listOf(VenueMeta(slug = freetraining.venueSlug, plan = null)),
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
    planId = plan.id,
)
