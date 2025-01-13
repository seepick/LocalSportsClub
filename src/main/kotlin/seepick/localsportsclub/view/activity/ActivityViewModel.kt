package seepick.localsportsclub.view.activity

import kotlinx.coroutines.flow.map
import seepick.localsportsclub.service.BookingService
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.view.shared.ScreenViewModel

class ActivityViewModel(
    clock: Clock,
    private val dataStorage: DataStorage,
    bookingService: BookingService,
) : ScreenViewModel<Activity, ActivitySearch>(dataStorage, bookingService) {

    override val tableColumns = activitiesTableColumns(clock)
    override val selectedItem = selectedSubEntity.map { it?.maybeActivity }
    override val selectedVenue = selectedVenueBySelectedSubEntity

    override fun buildSearch(resetItems: () -> Unit) = ActivitySearch(dataStorage.activitiesCategories, resetItems)
    override fun DataStorage.selectAllItems() = selectVisibleActivities()

    override fun onActivitiesAdded(activities: List<Activity>) {
        onItemsAdded(activities)
    }

    override fun onActivitiesDeleted(activities: List<Activity>) {
        onItemsDeleted(activities)
    }
}
