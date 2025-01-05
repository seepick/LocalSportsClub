package seepick.localsportsclub.view.freetraining

import seepick.localsportsclub.service.BookingService
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.view.shared.ScreenViewModel

class FreetrainingViewModel(
    clock: Clock,
    dataStorage: DataStorage,
    bookingService: BookingService,
) : ScreenViewModel<Freetraining, FreetrainingsSearch>(dataStorage, bookingService) {

    override val tableColumns = freetrainingsTableColumns(clock)
    override val selectedItem = selectedFreetraining
    override val selectedVenue = selectedVenueBySelectedItem

    override fun buildSearch(resetItems: () -> Unit) = FreetrainingsSearch(resetItems)
    override fun DataStorage.selectAllItems() = selectVisibleFreetrainings()

    override fun onFreetrainingsAdded(freetrainings: List<Freetraining>) {
        onItemsAdded(freetrainings)
    }

    override fun onFreetrainingsDeleted(freetrainings: List<Freetraining>) {
        onItemsDeleted(freetrainings)
    }
}
