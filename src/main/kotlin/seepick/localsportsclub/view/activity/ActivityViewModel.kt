package seepick.localsportsclub.view.activity

import kotlinx.coroutines.flow.map
import seepick.localsportsclub.api.UscConfig
import seepick.localsportsclub.service.BookingService
import seepick.localsportsclub.service.SinglesService
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.view.shared.ScreenViewModel
import java.time.LocalDate

class ActivityViewModel(
    clock: Clock,
    private val dataStorage: DataStorage,
    bookingService: BookingService,
    uscConfig: UscConfig,
    singlesService: SinglesService,
) : ScreenViewModel<Activity, ActivitySearch>(dataStorage, bookingService, singlesService) {

    override val tableColumns = activitiesTableColumns(clock)
    override val selectedItem = selectedSubEntity.map { it?.maybeActivity }
    override val selectedVenue = selectedVenueBySelectedSubEntity
    override val showLinkedVenues = false
    override val initialSortColumn = tableColumns.single { it.headerLabel == "Date" }
    val syncDates: List<LocalDate>

    init {
        val today = clock.today()
        syncDates = (0..<uscConfig.syncDaysAhead).map { today.plusDays(it.toLong()) }
    }

    override fun buildSearch(resetItems: () -> Unit) = ActivitySearch(dataStorage.activitiesCategories, resetItems)
    override fun DataStorage.selectAllItems() = selectVisibleActivities()

    override fun onActivitiesAdded(activities: List<Activity>) {
        onItemsAdded(activities)
    }

    override fun onActivitiesDeleted(activities: List<Activity>) {
        onItemsDeleted(activities)
    }
}
