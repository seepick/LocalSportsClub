package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.booking.BookingResult
import seepick.localsportsclub.api.booking.CancelResult
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.SyncerListener
import seepick.localsportsclub.view.usage.UsageStorage

class BookingService(
    private val uscApi: UscApi,
    dataStorage: DataStorage,
    usageStorage: UsageStorage,
    private val activityRepo: ActivityRepo,
) {
    private val log = logger {}
    private val listeners: List<SyncerListener> = listOf(dataStorage, usageStorage)

    suspend fun book(activity: Activity): BookingResult =
        bookOrCancel(
            activity = activity,
            isBooking = true,
            apiOperation = UscApi::book,
            resultExtractor = { it is BookingResult.BookingSuccess },
        )

    suspend fun cancel(activity: Activity): CancelResult =
        bookOrCancel(
            activity = activity,
            isBooking = false,
            apiOperation = UscApi::cancel,
            resultExtractor = { it is CancelResult.CancelSuccess },
        )

    private suspend fun <T> bookOrCancel(
        activity: Activity,
        isBooking: Boolean,
        apiOperation: suspend UscApi.(Int) -> T,
        resultExtractor: (T) -> Boolean,
    ): T {
        require(activity.isBooked == !isBooking)
        log.info { "${if (isBooking) "Booking" else "Cancel"} started for: $activity" }
        val result = uscApi.apiOperation(activity.id)
        if (resultExtractor(result)) {
            val activityDbo = activityRepo.selectById(activity.id)!!
            require(activityDbo.isBooked != isBooking)
            val updatedActivityDbo = activityDbo.copy(isBooked = isBooking)
            activityRepo.update(updatedActivityDbo)
            listeners.forEach {
                it.onActivityDboUpdated(updatedActivityDbo, ActivityFieldUpdate.IsBooked)
            }
        }
        return result
    }
}
