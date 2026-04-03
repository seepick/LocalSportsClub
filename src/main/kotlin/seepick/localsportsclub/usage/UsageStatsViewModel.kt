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
import seepick.localsportsclub.service.model.Category
import seepick.localsportsclub.service.singles.SinglesService
import java.time.LocalDate


class UsageStatsViewModel(
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val venueRepo: VenueRepo,
    private val clock: Clock,
    private val singlesService: SinglesService,
) : ViewModel() {

    private val numberOfRecentPenalties = 3
    private val numberOfTopCategories = 3
    private val numberOfTopVenues = 3
    val values by lazy { loadValues() }

    private fun loadValues(): StatsValues {
        val venues = venueRepo.selectAllAnywhere().associateBy { it.id }
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
            topVenues = loadTopVenueCheckins(venues, checkedinActivities),
            monthlyVenueCheckins = loadMonthlyVenueCheckins(venues, checkedinActivities, checkedinFreetrainings),
            firstCheckinDate = checkedinActivities.minBy { it.from }.from.toLocalDate(),
        )
    }

    private fun loadTopVenueCheckins(
        venues: Map<Int, VenueDbo>,
        checkedinActivities: List<ActivityDbo>,
    ): List<VenueCheckin> =
        checkedinActivities
            .groupBy { it.venueId }
            .map { VenueCheckin(venue = venues[it.key]!!, checkinsCount = it.value.size, maxCheckinsMonth = null) }
            .sortedByDescending { it.checkinsCount }
            .take(numberOfTopVenues)

    private fun loadMonthlyVenueCheckins(
        venues: Map<Int, VenueDbo>,
        checkedinActivities: List<ActivityDbo>,
        checkedinFreetrainings: List<FreetrainingDbo>,
    ): List<VenueCheckin> {
        val now = clock.today()
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

class CategoryCount(
    category: String,
    val checkinsCount: Int,
) {
    val category = Category(category, rating = null)
}

data class VenueCheckin(
    val venue: VenueDbo,
    val checkinsCount: Int,
    val maxCheckinsMonth: Int?,
)

data class StatsValues(
    val totalCheckins: Int,
    val penalties: List<ActivityDbo>,
    val topCategories: List<CategoryCount>,
    val topVenues: List<VenueCheckin>,
    val monthlyVenueCheckins: List<VenueCheckin>, // per month, NOT period (max 6)
    val firstCheckinDate: LocalDate,
) {
    companion object
}
