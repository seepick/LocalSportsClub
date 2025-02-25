package seepick.localsportsclub.view

import androidx.lifecycle.ViewModel
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.sameYearMonth
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.singles.SinglesService

class StatsViewModel(
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val venueRepo: VenueRepo,
    private val clock: Clock,
    private val singlesService: SinglesService,
) : ViewModel() {

    val values by lazy {
        loadValues()
    }

    private fun loadValues(): StatsValues {
        val activities = activityRepo.selectAllAnywhere()
        val freetrainings = freetrainingRepo.selectAllAnywhere()
        val venues = venueRepo.selectAllAnywhere().associateBy { it.id }
        val checkedinActivities = activities.filter { it.isCheckedin }
        val checkedinFreetrainings = freetrainings.filter { it.isCheckedin }

        val penalties =
            (activities.filter { it.state == ActivityState.Noshow } + activities.filter { it.state == ActivityState.CancelledLate })
                .sortedByDescending { it.from }.take(3)

        val topCategories =
            checkedinActivities.groupingBy { it.category }.eachCount().map { CategoryCount(it.key, it.value) }
                .sortedByDescending { it.checkinsCount }.take(3)

        val now = clock.today()
        val venueCheckins =
            ((checkedinActivities.filter { it.from.sameYearMonth(now) }.groupingBy { it.venueId }.eachCount()
                .map { VenueCheckin(venues[it.key]!!, it.value) }) +
                    (checkedinFreetrainings.filter { it.date.sameYearMonth(now) }.groupingBy { it.venueId }
                        .eachCount().map { VenueCheckin(venues[it.key]!!, it.value) }))
                .sortedByDescending { it.checkinsCount }

        return StatsValues(
            totalCheckins = checkedinActivities.size + checkedinFreetrainings.size,
            penalties = penalties,
            topCategories = topCategories,
            venueCheckins = venueCheckins,
            maxVenueCheckins = singlesService.plan?.usageInfo?.maxCheckinsInMonthPerVenue
        )
    }
}

data class CategoryCount(
    val category: String,
    val checkinsCount: Int,
)

data class VenueCheckin(
    val venue: VenueDbo,
    val checkinsCount: Int,
)

data class StatsValues(
    val totalCheckins: Int,
    val penalties: List<ActivityDbo>,
    val topCategories: List<CategoryCount>,
    val venueCheckins: List<VenueCheckin>, // per month, NOT period (max 6)
    val maxVenueCheckins: Int?,
) {
    companion object {
        val dummy = StatsValues(
            totalCheckins = 42,
            penalties = emptyList(),
            topCategories = emptyList(),
            venueCheckins = listOf(VenueCheckin(VenueDbo.dummy, 3)),
            maxVenueCheckins = 6,
        )
    }
}
