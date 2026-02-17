package seepick.localsportsclub.sync

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.checkin.ActivityCheckinEntry
import com.github.seepick.uscclient.checkin.ActivityCheckinEntryType
import com.github.seepick.uscclient.checkin.CheckinEntry
import com.github.seepick.uscclient.checkin.FreetrainingCheckinEntry
import com.github.seepick.uscclient.model.City
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.ActivityState
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
    private val clock: Clock,
) {
    private val log = logger {}

    suspend fun sync(city: City) {
        log.debug { "Syncing checkins..." }
        progress.onProgressCheckins(null)
        val entries = fetchEntries()
        entries.map { entry ->
            when (entry) {
                is ActivityCheckinEntry -> processActivity(city, entry)
                is FreetrainingCheckinEntry -> processFreetraining(city, entry)
            }
        }
    }

    private suspend fun fetchEntries(): MutableList<CheckinEntry> {
        val newestLocalDate = activityRepo.selectNewestCheckedinDate()

        val entries = mutableListOf<CheckinEntry>()
        var oldestRemoteDate: LocalDate?
        var currentPage = 1
        do {
            progress.onProgressCheckins("Page $currentPage")
            val page = uscApi.fetchCheckinsPage(currentPage, clock.today())
            entries += page.entries

            oldestRemoteDate = page.entries.minOfOrNull { it.date }
            println("page $currentPage; oldestRemoteDate=$oldestRemoteDate; newestLocalDate=$newestLocalDate")
            if (oldestRemoteDate != null && newestLocalDate != null && oldestRemoteDate < newestLocalDate) {
                log.trace { "Reached checked-in remote entries older ($oldestRemoteDate) than the newest locally stored ($newestLocalDate); stop syncing." }
                break
            }
            currentPage++
        } while (!page.isEmpty)
        return entries
    }

    private fun ActivityCheckinEntryType.toActivityState() = when (this) {
        ActivityCheckinEntryType.Checkedin -> ActivityState.Checkedin
        ActivityCheckinEntryType.Noshow -> ActivityState.Noshow
        ActivityCheckinEntryType.CancelledLate -> ActivityState.CancelledLate
    }

    private suspend fun processActivity(city: City, entry: ActivityCheckinEntry) {
        val activity = activityRepo.selectById(entry.activityId) ?: dataSyncRescuer.fetchInsertAndDispatchActivity(
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

    private suspend fun processFreetraining(city: City, entry: FreetrainingCheckinEntry) {
        val freetraining =
            freetrainingRepo.selectById(entry.freetrainingId) ?: dataSyncRescuer.fetchInsertAndDispatchFreetraining(
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
