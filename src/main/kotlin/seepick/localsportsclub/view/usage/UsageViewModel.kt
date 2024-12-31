package seepick.localsportsclub.view.usage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

class UsageViewModel(
    clock: Clock,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    uscConfig: UscConfig,
) : ViewModel(), SyncerListener, ApplicationLifecycleListener {

    private val config = uscConfig.usageConfig

    private val checkedinActivityIdsFlow = MutableStateFlow(emptySet<Int>())
    private val checkedinFreetrainingIdsFlow = MutableStateFlow(emptySet<Int>())

    val checkedinCount: StateFlow<Int> =
        combine(checkedinActivityIdsFlow, checkedinFreetrainingIdsFlow) { activityIds, freetrainingIds ->
            println("combine result: $activityIds + $freetrainingIds")
            activityIds.size + freetrainingIds.size
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    var bookedCount by mutableStateOf(0)

    // testing visible
    val periodFirstDay: LocalDate
    val periodLastDay: LocalDate

    init {
        val today = clock.today()
        val periodPivot = today.withDayOfMonth(config.periodAlwaysFirstDay)
        periodFirstDay = if (periodPivot <= today) periodPivot else periodPivot.minusMonths(1)
        periodLastDay = if (periodPivot <= today) periodPivot.plusMonths(1).minusDays(1) else periodPivot.minusDays(1)
    }

    override fun onStartUp() {
        activityRepo.selectAll().forEach(::processActivity)
//        freetrainingRepo.selectAll()
    }

    override fun onVenueDboAdded(venueDbo: VenueDbo) {
        // no-op
    }

    private fun processActivity(activityDbo: ActivityDbo) {
        if (activityDbo.wasCheckedin && activityDbo.from.toLocalDate() >= periodFirstDay && activityDbo.to.toLocalDate() <= periodLastDay) {
            addCheckedinActivityId(activityDbo.id)
        }
    }

    override fun onActivityDbosAdded(activityDbos: List<ActivityDbo>) {
        activityDbos.forEach(::processActivity)
    }

    override fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
        if (field == ActivityFieldUpdate.WasCheckedin) {
            if (activityDbo.wasCheckedin) addCheckedinActivityId(activityDbo.id)
            else removeCheckedinActivityId(activityDbo.id)
        }
    }

    private fun addCheckedinActivityId(id: Int) {
        checkedinActivityIdsFlow.update {
            println("adding: $id")
            it.toMutableSet() + id
        }
    }

    private fun removeCheckedinActivityId(id: Int) {
        checkedinActivityIdsFlow.update {
            println("removing: $id")
            it.toMutableSet() - id
        }
    }

    override fun onFreetrainingDbosAdded(freetrainingDbos: List<FreetrainingDbo>) {
    }

    override fun onFreetrainingDboUpdated(freetrainingDbo: FreetrainingDbo, field: FreetrainingFieldUpdate) {
    }

}
