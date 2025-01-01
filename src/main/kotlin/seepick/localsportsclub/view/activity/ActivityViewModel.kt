package seepick.localsportsclub.view.activity

import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.view.shared.ScreenViewModel

class ActivityViewModel(
    clock: Clock,
    dataStorage: DataStorage,
) : ScreenViewModel<Activity, ActivitySearch>(dataStorage) {

    override val tableColumns = activitiesTableColumns(clock)
    override val selectedItem = selectedActivity
    override val selectedVenue = selectedVenueBySelectedItem

    override fun buildSearch(resetItems: () -> Unit) = ActivitySearch(resetItems)
    override fun DataStorage.selectAllItems() = selectVisibleActivities()
    override fun onActivitiesAdded(activities: List<Activity>) {
        activities.forEach { activity ->
            onItemAdded(activity)
        }
    }
}
