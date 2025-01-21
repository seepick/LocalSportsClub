package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.PhpSessionProvider
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.booking.BookingResult
import seepick.localsportsclub.api.booking.CancelResult
import seepick.localsportsclub.gcal.GcalDeletion
import seepick.localsportsclub.gcal.GcalEntry
import seepick.localsportsclub.gcal.GcalService
import seepick.localsportsclub.gcal.readCalendarIdOrThrow
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.date.DateTimeRange
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.FreetrainingFieldUpdate
import seepick.localsportsclub.sync.SyncerListener
import seepick.localsportsclub.view.shared.SubEntity

class BookingService(
    private val uscApi: UscApi,
    private val activityRepo: ActivityRepo,
    private val venueRepo: VenueRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val gcalService: GcalService,
    private val phpSessionProvider: PhpSessionProvider,
    private val singlesService: SinglesService
) {
    private val log = logger {}

    private val listeners = mutableListOf<SyncerListener>()

    fun registerListener(listener: SyncerListener) {
        log.debug { "Registering listener: ${listener::class.qualifiedName}" }
        listeners += listener
    }

    suspend fun book(subEntity: SubEntity): BookingResult = bookOrCancel(
        subEntity = subEntity,
        isBooking = true,
        apiOperation = { uscApi.book(phpSessionProvider.provide(), it) },
        operationSucceeded = { it is BookingResult.BookingSuccess },
    )

    suspend fun cancel(subEntity: SubEntity): CancelResult = bookOrCancel(
        subEntity = subEntity,
        isBooking = false,
        apiOperation = { uscApi.cancel(phpSessionProvider.provide(), it) },
        operationSucceeded = { it is CancelResult.CancelSuccess },
    )

    private val SubEntity.isBookable: Boolean
        get() = when (this) {
            is SubEntity.ActivityEntity -> activity.state == ActivityState.Blank
            is SubEntity.FreetrainingEntity -> freetraining.state == FreetrainingState.Blank
        }
    private val SubEntity.isCancellable: Boolean
        get() = when (this) {
            is SubEntity.ActivityEntity -> activity.state == ActivityState.Booked
            is SubEntity.FreetrainingEntity -> freetraining.state == FreetrainingState.Scheduled
        }

    private val ActivityDbo.isBookable: Boolean get() = state == ActivityState.Blank
    private val ActivityDbo.isCancellable: Boolean get() = state == ActivityState.Booked
    private val FreetrainingDbo.isSchedulable: Boolean get() = state == FreetrainingState.Blank
    private val FreetrainingDbo.isCancellable: Boolean get() = state == FreetrainingState.Scheduled
    private fun ActivityDbo.Companion.bookingState(isBooking: Boolean) =
        if (isBooking) ActivityState.Booked else ActivityState.Blank

    private fun FreetrainingDbo.Companion.bookingState(isBooking: Boolean) =
        if (isBooking) FreetrainingState.Scheduled else FreetrainingState.Blank

    private suspend fun <T> bookOrCancel(
        subEntity: SubEntity,
        isBooking: Boolean,
        apiOperation: suspend UscApi.(Int) -> T,
        operationSucceeded: (T) -> Boolean,
    ): T {
        log.debug { "book=$isBooking => $subEntity" }
        require(if (isBooking) subEntity.isBookable else subEntity.isCancellable)
        log.info { "${if (isBooking) "Booking" else "Cancel"} started for: $subEntity" }
        val result = uscApi.apiOperation(subEntity.id)
        if (operationSucceeded(result)) {
            when (subEntity) {
                is SubEntity.ActivityEntity -> {
                    bookOrCancelActivity(subEntity, isBooking)
                }

                is SubEntity.FreetrainingEntity -> {
                    bookOrCancelFreetraining(subEntity, isBooking)
                }
            }
        }
        return result
    }

    private fun bookOrCancelActivity(subEntity: SubEntity.ActivityEntity, isBooking: Boolean) {
        val activityDbo = activityRepo.selectById(subEntity.id)!!
        require(if (isBooking) activityDbo.isBookable else activityDbo.isCancellable)
        val updatedActivityDbo = activityDbo.copy(state = ActivityDbo.bookingState(isBooking))
        activityRepo.update(updatedActivityDbo)
        if (isBooking) {
            createCalendarActivity(updatedActivityDbo)
        } else {
            val calendarId = singlesService.preferences.gcal.maybeCalendarId ?: error("No calendar ID set!")
            gcalService.delete(
                calendarId = calendarId, GcalDeletion(
                    day = subEntity.activity.dateTimeRange.from.toLocalDate(),
                    activityOrFreetrainingId = subEntity.activity.id,
                    isActivity = true,
                )
            )
        }
        listeners.forEach {
            it.onActivityDboUpdated(updatedActivityDbo, ActivityFieldUpdate.State)
        }
    }

    private fun VenueDbo.location() = "${name}, $street, $postalCode $addressLocality"

    private fun createCalendarActivity(activityDbo: ActivityDbo) {
        val venue = venueRepo.selectById(activityDbo.venueId) ?: error("Venue not found for: $activityDbo")
        gcalService.create(singlesService.readCalendarIdOrThrow(),
            GcalEntry.GcalActivity(
                activityId = activityDbo.id,
                title = activityDbo.nameWithTeacherIfPresent,
                dateTimeRange = DateTimeRange(from = activityDbo.from, to = activityDbo.to),
                location = venue.location(),
                notes = "[LSC] created${
                    venue.officialWebsite?.let { "\n$it" } ?: ""
                }",
            ))
    }

    private fun bookOrCancelFreetraining(subEntity: SubEntity.FreetrainingEntity, isBooking: Boolean) {
        val freetrainingDbo = freetrainingRepo.selectById(subEntity.id)!!
        require(if (isBooking) freetrainingDbo.isSchedulable else freetrainingDbo.isCancellable)
        val updatedFreetrainingDbo = freetrainingDbo.copy(state = FreetrainingDbo.bookingState(isBooking))
        freetrainingRepo.update(updatedFreetrainingDbo)
        if (isBooking) {
            createCalendarFreetraining(updatedFreetrainingDbo)
        } else {
            gcalService.delete(
                singlesService.readCalendarIdOrThrow(), GcalDeletion(
                    day = subEntity.freetraining.date,
                    activityOrFreetrainingId = subEntity.freetraining.id,
                    isActivity = false,
                )
            )
        }
        listeners.forEach {
            it.onFreetrainingDboUpdated(updatedFreetrainingDbo, FreetrainingFieldUpdate.State)
        }
    }

    private fun createCalendarFreetraining(freetrainingDbo: FreetrainingDbo) {
        val venue = venueRepo.selectById(freetrainingDbo.venueId) ?: error("Venue not found for: $freetrainingDbo")
        gcalService.create(singlesService.readCalendarIdOrThrow(),
            GcalEntry.GcalFreetraining(
                freetrainingId = freetrainingDbo.id,
                title = freetrainingDbo.name,
                date = freetrainingDbo.date,
                location = venue.location(),
                notes = "[LSC] created${
                    venue.officialWebsite?.let { "\n$it" } ?: ""
                }",
            ))
    }

    fun markActivityFromNoshowToCheckedin(activity: Activity) {
        log.debug { "markActivityFromNoshowToCheckedin($activity)" }
        require(activity.state == ActivityState.Noshow)
        val updated = activityRepo.selectById(activity.id)!!.copy(state = ActivityState.Checkedin)
        activityRepo.update(updated)
        listeners.forEach {
            it.onActivityDboUpdated(updated, ActivityFieldUpdate.State)
        }
    }
}

