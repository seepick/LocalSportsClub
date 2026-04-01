package seepick.localsportsclub.service

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.booking.BookingResult
import com.github.seepick.uscclient.booking.CancelResult
import io.github.oshai.kotlinlogging.KotlinLogging.logger
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
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.FreetrainingFieldUpdate
import seepick.localsportsclub.sync.SyncerListener
import seepick.localsportsclub.view.shared.SubEntity

class BookingService(
    private val uscApi: UscApi,
    private val venueRepo: VenueRepo,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val gcalService: GcalService,
    private val singlesService: SinglesService,
    private val activityDetailService: ActivityDetailService,
) {
    private val log = logger {}

    private val listeners = mutableListOf<SyncerListener>()

    fun registerListener(listener: SyncerListener) {
        log.debug { "Registering listener: ${listener::class.qualifiedName}" }
        listeners += listener
    }

    suspend fun book(subEntity: SubEntity, canGcal: Boolean, shouldGcal: Boolean): BookingResult = bookOrCancel(
        subEntity = subEntity,
        isBooking = true,
        canGcal = canGcal,
        shouldGcal = shouldGcal,
        apiOperation = { uscApi.book(it) },
        operationSucceeded = { it is BookingResult.BookingSuccess },
    )

    suspend fun cancel(subEntity: SubEntity, canGcal: Boolean, shouldGcal: Boolean): CancelResult = bookOrCancel(
        subEntity = subEntity,
        isBooking = false,
        canGcal = canGcal,
        shouldGcal = shouldGcal,
        apiOperation = { uscApi.cancel(it) },
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
        canGcal: Boolean,
        shouldGcal: Boolean,
        apiOperation: suspend UscApi.(Int) -> T,
        operationSucceeded: (T) -> Boolean,
    ): T {
        log.info { "${if (isBooking) "Booking" else "Cancel"} started (Gcal: can=$canGcal/should=$shouldGcal) for: $subEntity" }
        require(if (isBooking) subEntity.isBookable else subEntity.isCancellable)

        if (isBooking && subEntity is SubEntity.ActivityEntity && subEntity.activity.teacher == null) {
            activityDetailService.syncSingle(subEntity.activity)
            // the activity has mutable state which will be communicated through to below ;)
        }

        val result = uscApi.apiOperation(subEntity.id)
        if (operationSucceeded(result)) {
            when (subEntity) {
                is SubEntity.ActivityEntity -> {
                    bookOrCancelActivity(subEntity, isBooking, canGcal, shouldGcal)
                }

                is SubEntity.FreetrainingEntity -> {
                    bookOrCancelFreetraining(subEntity, isBooking, shouldGcal)
                }
            }
        }
        return result
    }

    private fun bookOrCancelActivity(
        subEntity: SubEntity.ActivityEntity,
        isBooking: Boolean,
        canGcal: Boolean,
        shouldGcal: Boolean,
    ) {
        val activityDbo =
            activityRepo.selectById(subEntity.id) ?: error("Cannot find entity by ID from repository: $subEntity")
        require(if (isBooking) activityDbo.isBookable else activityDbo.isCancellable)
        val updatedActivityDbo = activityDbo.copy(state = ActivityDbo.bookingState(isBooking))
        activityRepo.update(updatedActivityDbo)
        if (canGcal && shouldGcal) {
            if (isBooking) {
                createCalendarActivity(subEntity.activity)
            } else {
                deleteCalendarActivity(subEntity.activity)
            }
        }
        listeners.forEach {
            it.onActivityDboUpdated(updatedActivityDbo, ActivityFieldUpdate.State(oldState = activityDbo.state))
        }
    }

    private fun createCalendarActivity(activity: Activity) {
        gcalService.create(
            singlesService.readCalendarIdOrThrow(), GcalEntry.GcalActivity(
                activityId = activity.id,
                title = activity.gcalEntryTitle(),
                dateTimeRange = activity.dateTimeRange,
                location = activity.venue.gcalEntryLocation(),
                notes = activity.gcalEntryNotes(),
            )
        )
    }


    private fun deleteCalendarActivity(activity: Activity) {
        val calendarId = singlesService.preferences.gcal.maybeCalendarId ?: error("No calendar ID set!")
        gcalService.delete(
            calendarId = calendarId,
            deletion = GcalDeletion(
                day = activity.dateTimeRange.from.toLocalDate(),
                activityOrFreetrainingId = activity.id,
                isActivity = true,
            ),
        )
    }

    private fun bookOrCancelFreetraining(
        subEntity: SubEntity.FreetrainingEntity,
        isBooking: Boolean,
        manageGcal: Boolean,
    ) {
        val freetrainingDbo = freetrainingRepo.selectById(subEntity.id)!!
        require(if (isBooking) freetrainingDbo.isSchedulable else freetrainingDbo.isCancellable)
        val updatedFreetrainingDbo = freetrainingDbo.copy(state = FreetrainingDbo.bookingState(isBooking))
        freetrainingRepo.update(updatedFreetrainingDbo)
        if (manageGcal) {
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
        }
        listeners.forEach {
            it.onFreetrainingDboUpdated(updatedFreetrainingDbo, FreetrainingFieldUpdate.State)
        }
    }

    private fun createCalendarFreetraining(freetrainingDbo: FreetrainingDbo) {
        val venue = venueRepo.selectById(freetrainingDbo.venueId) ?: error("Venue not found for: $freetrainingDbo")
        gcalService.create(
            singlesService.readCalendarIdOrThrow(), GcalEntry.GcalFreetraining(
                freetrainingId = freetrainingDbo.id,
                title = freetrainingDbo.name,
                date = freetrainingDbo.date,
                location = venue.gcalEntryLocation(),
                notes = buildString {
                    append("[LSC] created")
                    venue.officialWebsite?.also { append("\n$it") }
                },
            )
        )
    }

    fun changeActivityToCheckedin(activity: Activity) {
        log.debug { "changeActivityToCheckedin($activity)" }
        require(activity.state != ActivityState.Checkedin) // no-show, or cancelled-late
        val oldDbo = activityRepo.selectById(activity.id)!!
        val updated = oldDbo.copy(state = ActivityState.Checkedin)
        activityRepo.update(updated)
        listeners.forEach {
            it.onActivityDboUpdated(updated, ActivityFieldUpdate.State(oldState = oldDbo.state))
        }
    }
}

private fun Activity.gcalEntryTitle() = buildString {
    remark?.also { append("${it.rating.emoji} ") }
    category.emoji?.also { append("$it ") }
    append(name)
    teacher?.also { append(" /$it") }
    teacherRemark?.also { append(" ${it.rating.emoji}") }
}

private fun Activity.gcalEntryNotes() = buildString {
    append("Created by LSC 🧙🏻‍♂️")
    venue.officialWebsite?.also { append("\n$it") }
    teacherRemark?.also { append("\n${teacher}: ${it.remark}") }
}

private fun VenueDbo.gcalEntryLocation() = "${name}, $street, $postalCode $addressLocality"
private fun Venue.gcalEntryLocation() = "${name}, $street, $postalCode $addressLocality"
