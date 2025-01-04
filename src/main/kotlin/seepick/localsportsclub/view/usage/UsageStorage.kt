package seepick.localsportsclub.view.usage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.sync.ActivityFieldUpdate
import seepick.localsportsclub.sync.FreetrainingFieldUpdate
import seepick.localsportsclub.sync.SyncerListener
import java.time.LocalDate

class UsageStorage(
    clock: Clock,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    uscConfig: UscConfig,
) : SyncerListener, ApplicationLifecycleListener {

    private val config = uscConfig.usageConfig

    private val checkedinActivityIds = MutableStateFlow(emptySet<Int>())
    private val checkedinFreetrainingIds = MutableStateFlow(emptySet<Int>())
    private val bookedActivityIds = MutableStateFlow(emptySet<Int>())

    val checkedinCount: Flow<Int> =
        combine(checkedinActivityIds, checkedinFreetrainingIds) { activityIds, freetrainingIds ->
            activityIds.size + freetrainingIds.size
        }
    val bookedCount: Flow<Int> = bookedActivityIds.map { it.size }

    val percentageCheckedin = checkedinCount.map {
        it / config.maxBookingsForPeriod.toDouble()
    }
    val percentageBooked = bookedCount.map {
        it / config.maxBookingsForPeriod.toDouble()
    }

    // visible for testing
    val periodFirstDay: LocalDate
    val periodLastDay: LocalDate // inclusive
    private val periodRange: ClosedRange<LocalDate>
    val percentagePeriod: Double

    init {
        val today = clock.today()
        val periodPivot = today.withDayOfMonth(config.periodConfiguredFirstDay)
        periodFirstDay = if (periodPivot <= today) periodPivot else periodPivot.minusMonths(1)
        periodLastDay = if (periodPivot <= today) periodPivot.plusMonths(1).minusDays(1) else periodPivot.minusDays(1)
        periodRange = periodFirstDay..periodLastDay

        val totalDaysForPeriod = periodFirstDay.until(periodLastDay).days + 1 // 30 or 31
        val daysPassedForPeriod = periodFirstDay.until(today).days
        percentagePeriod = daysPassedForPeriod.toDouble() / totalDaysForPeriod
    }

    override fun onStartUp() {
        activityRepo.selectAll().forEach(::processActivity)
        freetrainingRepo.selectAll().forEach(::processFreetraining)
    }

    override fun onVenueDboAdded(venueDbo: VenueDbo) {
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
            ActivityFieldUpdate.IsBooked -> {
                if (activityDbo.isBooked) bookedActivityIds.update { it + activityDbo.id }
                else bookedActivityIds.update { it - activityDbo.id }
            }

            ActivityFieldUpdate.WasCheckedin -> {
                if (activityDbo.wasCheckedin) checkedinActivityIds.update { it + activityDbo.id }
                else checkedinActivityIds.update { it - activityDbo.id }
            }
        }
    }

    override fun onFreetrainingDboUpdated(freetrainingDbo: FreetrainingDbo, field: FreetrainingFieldUpdate) {
        if (freetrainingDbo.date !in periodRange) {
            return
        }
        when (field) {
            FreetrainingFieldUpdate.WasCheckedin -> {
                if (freetrainingDbo.wasCheckedin) checkedinFreetrainingIds.update { it + freetrainingDbo.id }
                else checkedinFreetrainingIds.update { it - freetrainingDbo.id }
            }
        }
    }

    override fun onActivityDbosDeleted(activityDbos: List<ActivityDbo>) {
        val inRange = activityDbos.filter { it.from.toLocalDate() in periodRange }
        checkedinActivityIds.update { ids -> ids - inRange.filter { it.wasCheckedin }.map { it.id }.toSet() }
        bookedActivityIds.update { ids -> ids - inRange.filter { it.isBooked }.map { it.id }.toSet() }
    }

    override fun onFreetrainingDbosDeleted(freetrainingDbos: List<FreetrainingDbo>) {
        checkedinFreetrainingIds.update { ids ->
            ids - freetrainingDbos.filter { it.date in periodRange && it.wasCheckedin }.map { it.id }.toSet()
        }
    }

    private fun processActivity(activityDbo: ActivityDbo) {
        if (activityDbo.from.toLocalDate() in periodRange) {
            if (activityDbo.wasCheckedin) checkedinActivityIds.update { it + activityDbo.id }
            if (activityDbo.isBooked) bookedActivityIds.update { it + activityDbo.id }
        }
    }

    private fun processFreetraining(freetrainingDbo: FreetrainingDbo) {
        if (freetrainingDbo.wasCheckedin && freetrainingDbo.date in periodRange) {
            checkedinFreetrainingIds.update { it + freetrainingDbo.id }
        }
    }
}
