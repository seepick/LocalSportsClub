package seepick.localsportsclub.usage

import androidx.lifecycle.ViewModel
import com.github.seepick.uscclient.venue.forPlanOrNull
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.sameYearMonth
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.singles.SinglesService


class UsageStatsViewModel(
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val venueRepo: VenueRepo,
    private val clock: Clock,
    private val singlesService: SinglesService,
) : ViewModel() {

    private val numberOfRecentPenalties = 3
    private val numberOfTopCategories = 5
    val values by lazy { loadValues() }

    private fun loadValues(): StatsValues {
        val activities = activityRepo.selectAllAnywhere()
        val checkedinActivities = activities.filter { it.isCheckedin }
        val checkedinFreetrainings = freetrainingRepo.selectAllAnywhere().filter { it.isCheckedin }
        return StatsValues(
            totalCheckins = checkedinActivities.size + checkedinFreetrainings.size,
            penalties = activities
                .filter { it.state == ActivityState.Noshow || it.state == ActivityState.CancelledLate }
                .sortedByDescending { it.from }
                .take(numberOfRecentPenalties),
            topCategories = checkedinActivities
                .groupingBy { it.category }
                .eachCount()
                .map { CategoryCount(it.key, it.value) }
                .sortedByDescending { it.checkinsCount }
                .take(numberOfTopCategories),
            venueCheckins = loadVenueCheckins(checkedinActivities, checkedinFreetrainings),
        )
    }

    private fun loadVenueCheckins(
        checkedinActivities: List<ActivityDbo>,
        checkedinFreetrainings: List<FreetrainingDbo>,
    ): List<VenueCheckin> {
        val now = clock.today()
        val venues = venueRepo.selectAllAnywhere().associateBy { it.id }
        return ((checkedinActivities.filter { it.from.sameYearMonth(now) }.groupingBy { it.venueId }.eachCount()
            .map {
                val venue = venues[it.key]!!
                VenueCheckin(
                    venue = venue,
                    checkinsCount = it.value,
                    maxCheckinsMonth = venue.visitLimits.forPlanOrNull(singlesService.plan),
                )
            }) +
                (checkedinFreetrainings.filter { it.date.sameYearMonth(now) }.groupingBy { it.venueId }
                    .eachCount().map {
                        val venue = venues[it.key]!!
                        VenueCheckin(
                            venue = venue,
                            checkinsCount = it.value,
                            maxCheckinsMonth = venue.visitLimits.forPlanOrNull(singlesService.plan),
                        )
                    }))
            .sortedByDescending { it.checkinsCount }
    }
}

data class CategoryCount(
    val category: String,
    val checkinsCount: Int,
)

data class VenueCheckin(
    val venue: VenueDbo,
    val checkinsCount: Int,
    val maxCheckinsMonth: Int?,
)

data class StatsValues(
    val totalCheckins: Int,
    val penalties: List<ActivityDbo>,
    val topCategories: List<CategoryCount>,
    val venueCheckins: List<VenueCheckin>, // per month, NOT period (max 6)
) {
    companion object
}
