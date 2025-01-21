package seepick.localsportsclub.view.usage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.service.SinglesService
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.FreetrainingFieldUpdate
import seepick.localsportsclub.sync.SyncerListener
import java.time.LocalDate

class UsageStorage(
    clock: Clock,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val singlesService: SinglesService,
) : SyncerListener, ApplicationLifecycleListener {

    val maxBookingsForPeriod by lazy {
        singlesService.plan?.usageInfo?.maxCheckins ?: 1
    }
    private val periodConfiguredFirstDay by lazy {
        singlesService.preferences.periodFirstDay ?: 1
    }

    private val bookedActivityIds = MutableStateFlow(emptySet<Int>())
    private val scheduledFreetrainingIds = MutableStateFlow(emptySet<Int>())
    private val checkedinActivityIds = MutableStateFlow(emptySet<Int>())
    private val checkedinFreetrainingIds = MutableStateFlow(emptySet<Int>())

    val bookedCount: Flow<Int> = combine(bookedActivityIds, scheduledFreetrainingIds) { activityIds, freetrainingIds ->
        activityIds.size + freetrainingIds.size
    }
    val checkedinCount: Flow<Int> =
        combine(checkedinActivityIds, checkedinFreetrainingIds) { activityIds, freetrainingIds ->
            activityIds.size + freetrainingIds.size
        }

    val percentageCheckedin: Flow<Double> = checkedinCount.map {
        it / maxBookingsForPeriod.toDouble()
    }
    val percentageBooked: Flow<Double> = bookedCount.map {
        it / maxBookingsForPeriod.toDouble()
    }

    // visible for testing
    val periodFirstDay: LocalDate
    val periodLastDay: LocalDate // inclusive
    private val periodRange: ClosedRange<LocalDate>
    val percentagePeriod: Double

    init {
        val today = clock.today()
        val periodPivot = today.withDayOfMonth(periodConfiguredFirstDay)
        periodFirstDay = if (periodPivot <= today) periodPivot else periodPivot.minusMonths(1)
        periodLastDay = if (periodPivot <= today) periodPivot.plusMonths(1).minusDays(1) else periodPivot.minusDays(1)
        periodRange = periodFirstDay..periodLastDay

        val totalDaysForPeriod = periodFirstDay.until(periodLastDay).days + 1 // 30 or 31
        val daysPassedForPeriod = periodFirstDay.until(today).days
        percentagePeriod = daysPassedForPeriod.toDouble() / totalDaysForPeriod
    }

    override fun onStartUp() {
        singlesService.preferences.city?.id?.let { cityId ->
            activityRepo.selectAll(cityId).forEach(::processActivity)
            freetrainingRepo.selectAll(cityId).forEach(::processFreetraining)
        }
    }

    override fun onVenueDbosAdded(venueDbos: List<VenueDbo>) {
        // no-op
    }

    override fun onActivityDbosAdded(activityDbos: List<ActivityDbo>) {
        activityDbos.forEach(::processActivity)
    }

    override fun onFreetrainingDbosAdded(freetrainingDbos: List<FreetrainingDbo>) {
        freetrainingDbos.forEach(::processFreetraining)
    }

    override fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        if (activityDbo.from.toLocalDate() !in periodRange) {
            return
        }
        when (field) {
            ActivityFieldUpdate.State -> {
                if (activityDbo.isBooked) {
                    bookedActivityIds.update { it + activityDbo.id }
                } else if (bookedActivityIds.value.contains(activityDbo.id)) {
                    bookedActivityIds.update { it - activityDbo.id }
                }
                if (activityDbo.isCheckedin) {
                    checkedinActivityIds.update { it + activityDbo.id }
                } else if (checkedinActivityIds.value.contains(activityDbo.id)) {
                    checkedinActivityIds.update { it - activityDbo.id }
                }
            }

            ActivityFieldUpdate.Teacher -> { /* no-op*/
            }
        }
    }

    override fun onFreetrainingDboUpdated(freetrainingDbo: FreetrainingDbo, field: FreetrainingFieldUpdate) {
        if (freetrainingDbo.date !in periodRange) {
            return
        }
        when (field) {
            FreetrainingFieldUpdate.State -> {
                if (freetrainingDbo.isScheduled) {
                    scheduledFreetrainingIds.update { it + freetrainingDbo.id }
                } else if (scheduledFreetrainingIds.value.contains(freetrainingDbo.id)) {
                    scheduledFreetrainingIds.update { it - freetrainingDbo.id }
                }
                if (freetrainingDbo.isCheckedin) {
                    checkedinFreetrainingIds.update { it + freetrainingDbo.id }
                } else if (checkedinFreetrainingIds.value.contains(freetrainingDbo.id)) {
                    checkedinFreetrainingIds.update { it - freetrainingDbo.id }
                }
            }
        }
    }

    override fun onActivityDbosDeleted(activityDbos: List<ActivityDbo>) {
        val inRange = activityDbos.filter { it.from.toLocalDate() in periodRange }
        checkedinActivityIds.update { ids -> ids - inRange.filter { it.isCheckedin }.map { it.id }.toSet() }
        bookedActivityIds.update { ids -> ids - inRange.filter { it.isBooked }.map { it.id }.toSet() }
    }

    override fun onFreetrainingDbosDeleted(freetrainingDbos: List<FreetrainingDbo>) {
        val inRange = freetrainingDbos.filter { it.date in periodRange }
        scheduledFreetrainingIds.update { ids -> ids - inRange.filter { it.isScheduled }.map { it.id }.toSet() }
        checkedinFreetrainingIds.update { ids -> ids - inRange.filter { it.isCheckedin }.map { it.id }.toSet() }
    }

    private fun processActivity(activityDbo: ActivityDbo) {
        if (activityDbo.from.toLocalDate() in periodRange) {
            if (activityDbo.isCheckedin) checkedinActivityIds.update { it + activityDbo.id }
            if (activityDbo.isBooked) bookedActivityIds.update { it + activityDbo.id }
        }
    }

    private fun processFreetraining(freetrainingDbo: FreetrainingDbo) {
        if (freetrainingDbo.date in periodRange) {
            if (freetrainingDbo.isScheduled) scheduledFreetrainingIds.update { it + freetrainingDbo.id }
            if (freetrainingDbo.isCheckedin) checkedinFreetrainingIds.update { it + freetrainingDbo.id }
        }
    }
}
