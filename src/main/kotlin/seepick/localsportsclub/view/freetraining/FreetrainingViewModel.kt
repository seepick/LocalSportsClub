package seepick.localsportsclub.view.freetraining

import kotlinx.coroutines.flow.map
import seepick.localsportsclub.api.UscConfig
import seepick.localsportsclub.service.BookingService
import seepick.localsportsclub.service.BookingValidator
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.view.SnackbarService
import seepick.localsportsclub.view.shared.ScreenViewModel
import seepick.localsportsclub.view.shared.SharedModel
import java.time.LocalDate

class FreetrainingViewModel(
    clock: Clock,
    private val dataStorage: DataStorage,
    bookingService: BookingService,
    uscConfig: UscConfig,
    singlesService: SinglesService,
    snackbarService: SnackbarService,
    sharedModel: SharedModel,
    bookingValidator: BookingValidator,
) : ScreenViewModel<Freetraining, FreetrainingSearch>(
    dataStorage,
    bookingService,
    singlesService,
    snackbarService,
    sharedModel,
    bookingValidator,
) {
    override val tableColumns = freetrainingsTableColumns(clock)
    override val selectedItem = selectedSubEntity.map { it?.maybeFreetraining }
    override val selectedVenue = selectedVenueBySelectedSubEntity
    override val showLinkedVenues = false
    override val initialSortColumn = tableColumns.single { it.headerLabel == "Date" }

    val syncDates: List<LocalDate>

    init {
        val today = clock.today()
        syncDates = (0..<uscConfig.syncDaysAhead).map { today.plusDays(it.toLong()) }
    }

    override fun buildSearch(resetItems: () -> Unit) =
        FreetrainingSearch(dataStorage.freetrainingsCategories, syncDates, resetItems)

    override fun DataStorage.selectAllItems() = selectVisibleFreetrainings()

    override fun onFreetrainingsAdded(freetrainings: List<Freetraining>) {
        onItemsAdded(freetrainings)
    }

    override fun onFreetrainingsDeleted(freetrainings: List<Freetraining>) {
        onItemsDeleted(freetrainings)
    }
}
