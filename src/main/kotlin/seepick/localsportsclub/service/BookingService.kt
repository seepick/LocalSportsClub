package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.booking.BookingResult
import seepick.localsportsclub.api.booking.CancelResult
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.FreetrainingFieldUpdate
import seepick.localsportsclub.sync.SyncerListener
import seepick.localsportsclub.view.shared.SubEntity
import seepick.localsportsclub.view.usage.UsageStorage

class BookingService(
    private val uscApi: UscApi,
    dataStorage: DataStorage,
    usageStorage: UsageStorage,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
) {
    private val log = logger {}
    private val listeners: List<SyncerListener> = listOf(dataStorage, usageStorage)

    suspend fun book(subEntity: SubEntity): BookingResult =
        bookOrCancel(
            subEntity = subEntity,
            isBooking = true,
            apiOperation = UscApi::book,
            operationSucceeded = { it is BookingResult.BookingSuccess },
        )

    suspend fun cancel(subEntity: SubEntity): CancelResult =
        bookOrCancel(
            subEntity = subEntity,
            isBooking = false,
            apiOperation = UscApi::cancel,
            operationSucceeded = { it is CancelResult.CancelSuccess },
        )

    private suspend fun <T> bookOrCancel(
        subEntity: SubEntity,
        isBooking: Boolean,
        apiOperation: suspend UscApi.(Int) -> T,
        operationSucceeded: (T) -> Boolean,
    ): T {
        require(subEntity.isBooked == !isBooking)
        log.info { "${if (isBooking) "Booking" else "Cancel"} started for: $subEntity" }
        val result = uscApi.apiOperation(subEntity.id)
        if (operationSucceeded(result)) {
            when (subEntity) {
                is SubEntity.ActivityEntity -> {
                    val activityDbo = activityRepo.selectById(subEntity.id)!!
                    require(activityDbo.isBooked != isBooking)
                    val updatedActivityDbo = activityDbo.copy(isBooked = isBooking)
                    activityRepo.update(updatedActivityDbo)
                    listeners.forEach {
                        it.onActivityDboUpdated(updatedActivityDbo, ActivityFieldUpdate.IsBooked)
                    }
                }

                is SubEntity.FreetrainingEntity -> {
                    val freetrainingDbo = freetrainingRepo.selectById(subEntity.id)!!
                    require(freetrainingDbo.isScheduled != isBooking)
                    val updatedFreetrainingDbo = freetrainingDbo.copy(isScheduled = isBooking)
                    freetrainingRepo.update(updatedFreetrainingDbo)
                    listeners.forEach {
                        it.onFreetrainingDboUpdated(updatedFreetrainingDbo, FreetrainingFieldUpdate.IsScheduled)
                    }
                }
            }
        }
        return result
    }
}
