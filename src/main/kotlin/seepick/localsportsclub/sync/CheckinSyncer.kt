package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.checkin.CheckinEntry
import seepick.localsportsclub.persistence.ActivityRepo
import java.time.LocalDate

class CheckinSyncer(
    private val uscApi: UscApi,
    private val activityRepo: ActivityRepo,
    private val dataSyncRescuer: DataSyncRescuer,
    private val dispatcher: SyncerListenerDispatcher,
) {
    private val log = logger {}

    suspend fun sync() {
        log.debug { "Syncing checkins..." }

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

        entries.map { entry ->
            activityRepo.selectById(entry.activityId) ?: dataSyncRescuer.rescueActivity(
                activityId = entry.activityId,
                venueSlug = entry.venueSlug,
                prefilledNotes = "[SYNC] rescued activity for past check-in"
            )
        }.filter { !it.wasCheckedin }.also { log.debug { "Going to mark ${it.size} activities as checked-in." } }
            .forEach { activity ->
                val updated = activity.copy(wasCheckedin = true)
                activityRepo.update(updated)
                dispatcher.dispatchOnActivityDboUpdated(updated, ActivityFieldUpdate.WasCheckedin)
            }
    }
}
