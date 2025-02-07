package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.monthRange
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.view.usage.UsageStorage

sealed interface BookingValidation {
    data object Valid : BookingValidation
    data class Invalid(val reason: String) : BookingValidation
}

class BookingValidator(
    singlesService: SinglesService,
    private val activityRepo: ActivityRepo,
    private val usageStorage: UsageStorage,
    private val clock: Clock,
) {

    private val log = logger {}
    private var bookedCount = 0
    private var checkedinCount = 0
    private val maxCheckinsInPeriod = singlesService.plan?.usageInfo?.maxCheckinsInPeriod
    private val maxCheckinsInMonthPerVenue = singlesService.plan?.usageInfo?.maxCheckinsInMonthPerVenue
    private val maxReservationsConcurrentlyTotal = singlesService.plan?.usageInfo?.maxReservationsConcurrentlyTotal
    private val maxReservationsConcurrentlyPerDay = singlesService.plan?.usageInfo?.maxReservationsConcurrentlyPerDay
    private val city = singlesService.preferences.city

    init {
        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch {
            usageStorage.bookedCount.collect {
                bookedCount = it
            }
        }
        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch {
            usageStorage.checkedinCount.collect {
                checkedinCount = it
            }
        }
    }

    fun canBook(activity: Activity): BookingValidation {
        if (!usageStorage.isUsageVisible) {
            // don't know any better...
            return BookingValidation.Valid
        }
        if (activity.dateTimeRange.from.toLocalDate() in usageStorage.periodRange) {
            if (checkedinCount >= maxCheckinsInPeriod!!) {
                return BookingValidation.Invalid("Maximum check-ins per period of $maxCheckinsInPeriod already exhausted!")
            }
        }
        val monthRange = clock.today().monthRange()
        val venueCheckinsThisMonth = activityRepo.selectAllForVenueId(activity.venue.id).count {
            it.isCheckedin && it.from.toLocalDate() in monthRange
        }
        if (venueCheckinsThisMonth >= maxCheckinsInMonthPerVenue!!) {
            return BookingValidation.Invalid("Cannot book more than $maxCheckinsInMonthPerVenue per month for venue '${activity.venue.name}'!")
        }

        val reservedTotal = activityRepo.selectAll(city!!.id).filter { it.isBooked }
        if (reservedTotal.count() >= maxReservationsConcurrentlyTotal!!) {
            return BookingValidation.Invalid("Cannot reserve more than $maxReservationsConcurrentlyTotal at the same time!")
        }
        val reservedToday = reservedTotal.filter { it.from.toLocalDate() == clock.today() }
        if (reservedToday.count() >= maxReservationsConcurrentlyPerDay!!) {
            return BookingValidation.Invalid("Cannot reserve more than $maxReservationsConcurrentlyPerDay at the same time per day!")
        }

        return BookingValidation.Valid
    }

    fun canCancel(activity: Activity): BookingValidation {
        if (activity.cancellationLimit == null) return BookingValidation.Valid
        return if (activity.cancellationLimit < clock.now()) {
            log.debug { "Cancellation limit of ${activity.cancellationLimit} is exceed, as now is: ${clock.now()}" }
            BookingValidation.Invalid("Cancellation limit exceeded. Fees will be charged if you cancel.")
        } else BookingValidation.Valid
    }
}
