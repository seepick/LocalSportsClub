package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.PhpSessionId
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.checkin.ActivityCheckinEntry
import seepick.localsportsclub.api.checkin.ActivityCheckinEntryType
import seepick.localsportsclub.api.checkin.CheckinEntry
import seepick.localsportsclub.api.checkin.FreetrainingCheckinEntry
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.FreetrainingState
import java.time.LocalDate

fun SyncProgress.onProgressCheckins(detail: String?) {
    onProgress("Check-ins", detail)
}

class CheckinSyncer(
    private val uscApi: UscApi,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val dataSyncRescuer: DataSyncRescuer,
    private val dispatcher: SyncerListenerDispatcher,
    private val progress: SyncProgress,
) {
    private val log = logger {}

    suspend fun sync(session: PhpSessionId, city: City) {
        log.debug { "Syncing checkins..." }
        progress.onProgressCheckins(null)
        val entries = fetchEntries(session)
        entries.map { entry ->
            when (entry) {
                is ActivityCheckinEntry -> processActivity(session, city, entry)
                is FreetrainingCheckinEntry -> processFreetraining(session, city, entry)
            }
        }
    }

    private suspend fun fetchEntries(session: PhpSessionId): MutableList<CheckinEntry> {
        val newestLocalDate = activityRepo.selectNewestCheckedinDate()

        var currentPage = 1
        val entries = mutableListOf<CheckinEntry>()
        var oldestRemoteDate: LocalDate?
        do {
            progress.onProgressCheckins("Page $currentPage")
            val page = uscApi.fetchCheckinsPage(session, currentPage)
            oldestRemoteDate = page.entries.minByOrNull { it.date }?.date
            entries += page.entries
            currentPage++
            if (oldestRemoteDate != null && newestLocalDate != null && oldestRemoteDate < newestLocalDate) {
                log.trace { "Reached checked-in remote entries older ($oldestRemoteDate) than the newest locally stored ($newestLocalDate); stop syncing." }
                break
            }
        } while (!page.isEmpty)
        return entries
    }

    private fun ActivityCheckinEntryType.toActivityState() = when (this) {
        ActivityCheckinEntryType.Checkedin -> ActivityState.Checkedin
        ActivityCheckinEntryType.Noshow -> ActivityState.Noshow
        ActivityCheckinEntryType.CancelledLate -> ActivityState.CancelledLate
    }

    private suspend fun processActivity(session: PhpSessionId, city: City, entry: ActivityCheckinEntry) {
        val activity = activityRepo.selectById(entry.activityId) ?: dataSyncRescuer.fetchInsertAndDispatchActivity(
            session = session,
            city = city,
            activityId = entry.activityId,
            venueSlug = entry.venueSlug,
            prefilledVenueNotes = "[SYNC] rescued activity for past check-in"
        )
        if (activity.state == entry.type.toActivityState()) {
            log.debug { "Activity was already marked as ${activity.state.name}, skipping: $entry" }
            return
        }
        val updated = activity.copy(
            state = entry.type.toActivityState()
        )
        activityRepo.update(updated)
        dispatcher.dispatchOnActivityDboUpdated(updated, ActivityFieldUpdate.State(oldState = activity.state))
    }

    private suspend fun processFreetraining(
        session: PhpSessionId,
        city: City,
        entry: FreetrainingCheckinEntry
    ) {
        val freetraining =
            freetrainingRepo.selectById(entry.freetrainingId) ?: dataSyncRescuer.fetchInsertAndDispatchFreetraining(
                session = session,
                city = city,
                freetrainingId = entry.freetrainingId,
                venueSlug = entry.venueSlug,
                prefilledNotes = "[SYNC] rescued freetraining for past check-in"
            )
        if (freetraining.isCheckedin) {
            log.debug { "Freetraining was already marked as checked-in, skipping: $entry" }
            return
        }
        val updated = freetraining.copy(state = FreetrainingState.Checkedin)
        freetrainingRepo.update(updated)
        dispatcher.dispatchOnFreetrainingDboUpdated(updated, FreetrainingFieldUpdate.State)
    }
}
