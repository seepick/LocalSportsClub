package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.checkin.ActivityCheckinEntry
import seepick.localsportsclub.api.checkin.CheckinEntry
import seepick.localsportsclub.api.checkin.FreetrainingCheckinEntry
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import java.time.LocalDate

class CheckinSyncer(
    private val uscApi: UscApi,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val dataSyncRescuer: DataSyncRescuer,
    private val dispatcher: SyncerListenerDispatcher,
) {
    private val log = logger {}

    suspend fun sync() {
        log.debug { "Syncing checkins..." }
        val entries = fetchEntries()
        entries.map { entry ->
            when (entry) {
                is ActivityCheckinEntry -> markActivityAsCheckedin(entry)
                is FreetrainingCheckinEntry -> markFreetrainingAsCheckedin(entry)
            }
        }
    }

    private suspend fun fetchEntries(): MutableList<CheckinEntry> {
        val newestLocalDate = activityRepo.selectNewestCheckedinDate()

        var currentPage = 1
        val entries = mutableListOf<CheckinEntry>()
        var oldestRemoteDate: LocalDate?
        do {
            val page = uscApi.fetchCheckinsPage(currentPage)
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

    private suspend fun markActivityAsCheckedin(entry: ActivityCheckinEntry) {
        val activity = activityRepo.selectById(entry.activityId) ?: dataSyncRescuer.fetchInsertAndDispatchActivity(
            activityId = entry.activityId,
            venueSlug = entry.venueSlug,
            prefilledNotes = "[SYNC] rescued activity for past check-in"
        )
        if (activity.state == ActivityState.Checkedin) {
            log.debug { "Activity was already marked as checked-in, skipping: $entry" }
            return
        }
        val updated = activity.copy(
            state = if (entry.isNoShow) ActivityState.Noshow else ActivityState.Checkedin
        )
        activityRepo.update(updated)
        dispatcher.dispatchOnActivityDboUpdated(updated, ActivityFieldUpdate.State)
    }

    private suspend fun markFreetrainingAsCheckedin(entry: FreetrainingCheckinEntry) {
        val freetraining =
            freetrainingRepo.selectById(entry.freetrainingId) ?: dataSyncRescuer.fetchInsertAndDispatchFreetraining(
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
