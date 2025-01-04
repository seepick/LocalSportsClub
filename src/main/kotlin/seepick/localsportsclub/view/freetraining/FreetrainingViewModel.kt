package seepick.localsportsclub.view.freetraining

import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.view.shared.ScreenViewModel

class FreetrainingViewModel(
    clock: Clock,
    dataStorage: DataStorage,
) : ScreenViewModel<Freetraining, FreetrainingsSearch>(dataStorage) {

    override val tableColumns = freetrainingsTableColumns(clock)
    override val selectedItem = selectedFreetraining
    override val selectedVenue = selectedVenueBySelectedItem

    override fun buildSearch(resetItems: () -> Unit) = FreetrainingsSearch(resetItems)
    override fun DataStorage.selectAllItems() = selectVisibleFreetrainings()

    override fun onFreetrainingsAdded(freetrainings: List<Freetraining>) {
        freetrainings.forEach { freetraining ->
            onItemAdded(freetraining)
        }
    }

    override fun onFreetrainingsDeleted(freetrainings: List<Freetraining>) {
        onItemsDeleted(freetrainings)
    }
}
