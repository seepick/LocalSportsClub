package seepick.localsportsclub.view.activity

import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.search.ActivitySearch
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.ScreenViewModel
import seepick.localsportsclub.view.common.table.CellRenderer.TextRenderer
import seepick.localsportsclub.view.common.table.ColSize
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.tableColumnFavorited
import seepick.localsportsclub.view.common.table.tableColumnVenueImage
import seepick.localsportsclub.view.common.table.tableColumnWishlisted

class ActivityViewModel(
    private val clock: Clock,
    dataStorage: DataStorage,
) : ScreenViewModel<Activity, ActivitySearch>(dataStorage) {

    override val tableColumns = listOf<TableColumn<Activity>>(
        tableColumnVenueImage { it.venue.imageFileName },
        TableColumn("Name", ColSize.Weight(0.5f), TextRenderer({ it.name }, { it.name.lowercase() })),
        TableColumn("Venue", ColSize.Weight(0.5f), TextRenderer { it.venue.name }),
        TableColumn("Date", ColSize.Width(200.dp), TextRenderer { it.dateTimeRange.prettyPrint(clock.today().year) }),
        TableColumn("Rating", ColSize.Width(120.dp), TextRenderer { it.venue.rating.string }),
        tableColumnFavorited { it.venue.isFavorited },
        tableColumnWishlisted { it.venue.isWishlisted },
        TableColumn("Bkd", ColSize.Width(30.dp), TextRenderer { if (it.isBooked) Icons.Lsc.booked else "" }),
        // TODO teacher, Checkins count
    )

    override val selectedItem = selectedActivity
    override val selectedVenue = selectedVenueBySelectedItem

    override fun buildSearch(resetItems: () -> Unit) = ActivitySearch(resetItems)

    override fun DataStorage.selectAllItems() =
        selectAllActivities()

    override fun onActivitiesAdded(activities: List<Activity>) {
        activities.forEach { activity ->
            onItemAdded(activity)
        }
    }
}

