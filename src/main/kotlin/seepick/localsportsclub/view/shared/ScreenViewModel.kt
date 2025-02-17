package seepick.localsportsclub.view.shared

import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.api.booking.BookingResult
import seepick.localsportsclub.api.booking.CancelResult
import seepick.localsportsclub.service.BookingService
import seepick.localsportsclub.service.BookingValidation
import seepick.localsportsclub.service.BookingValidator
import seepick.localsportsclub.service.SortingDelegate
import seepick.localsportsclub.service.findIndexFor
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.DataStorageListener
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.model.Gcal
import seepick.localsportsclub.service.model.HasVenue
import seepick.localsportsclub.service.model.NoopDataStorageListener
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.search.AbstractSearch
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.view.SnackbarEvent
import seepick.localsportsclub.view.SnackbarService
import seepick.localsportsclub.view.SnackbarType
import seepick.localsportsclub.view.common.CustomDialog
import seepick.localsportsclub.view.common.launchBackgroundTask
import seepick.localsportsclub.view.common.launchViewTask
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.TableNavigation
import seepick.localsportsclub.view.common.table.navigate
import seepick.localsportsclub.view.venue.VenueViewModel
import seepick.localsportsclub.view.venue.detail.VenueEditModel
import java.util.concurrent.atomic.AtomicBoolean

abstract class ScreenViewModel<ITEM : HasVenue, SEARCH : AbstractSearch<ITEM>>(
    private val dataStorage: DataStorage,
    private val bookingService: BookingService,
    private val singlesService: SinglesService,
    private val snackbarService: SnackbarService,
    private val sharedModel: SharedModel,
    private val bookingValidator: BookingValidator,
) : ViewModel(), DataStorageListener by NoopDataStorageListener, ApplicationLifecycleListener {

    private val log = logger {}

    abstract val tableColumns: List<TableColumn<ITEM>>

    private val _allItems = mutableStateListOf<ITEM>()
    val allItems: List<ITEM> = _allItems
    private val _items = mutableStateListOf<ITEM>()
    val items: List<ITEM> = _items
    abstract val selectedItem: Flow<ITEM?>

    abstract val selectedVenue: StateFlow<Venue?>
    val venueEdit = VenueEditModel()
    abstract val showLinkedVenues: Boolean

    var configuredCity: City? by mutableStateOf(null)

    private val _selectedSubEntity = MutableStateFlow<SubEntity?>(null)
    val selectedSubEntity = _selectedSubEntity.asStateFlow()

    abstract fun buildSearch(resetItems: () -> Unit): SEARCH
    val searching: SEARCH by lazy { buildSearch(::resetItems) }
    open val initialSortColumn: TableColumn<ITEM>? = null
    val sorting: SortingDelegate<ITEM> by lazy {
        SortingDelegate(
            tableColumns,
            initialSortColumn = initialSortColumn,
            resetSort = ::resetItems
        )
    }

    var isBookingOrCancelInProgress by mutableStateOf(false)
        private set
    var isBookOrCancelPossible by mutableStateOf(false)
        private set
    var isGcalEnabled by mutableStateOf(false)
        private set
    val shouldGcalBeManaged = sharedModel.shouldGcalBeManaged

    private var isAddingItems = AtomicBoolean(false)
    private var triedToResetItems = AtomicBoolean(false)

    override fun onStartUp() {
        log.info { "Filling initial data for: ${this::class.simpleName}" }

        val preferences = singlesService.preferences
        isBookOrCancelPossible = singlesService.verifiedUscCredentials != null
        isGcalEnabled = preferences.gcal is Gcal.GcalEnabled && singlesService.verifiedGcalId != null
        configuredCity = preferences.city

        _allItems.addAll(dataStorage.selectAllItems())
        sorting // trigger lazy
        searching.reset()
    }

    protected val selectedVenueBySelectedSubEntity: StateFlow<Venue?> by lazy {
        selectedSubEntity.map { item ->
            item?.venue?.also { venue ->
                venueEdit.init(venue)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
        // or with timeout: started = SharingStarted.WhileSubscribed(5_000L)
    }

    abstract fun DataStorage.selectAllItems(): List<ITEM>

    protected fun onItemsAdded(items: List<ITEM>) {
        isAddingItems.set(true)
        log.debug { "onItemsAdded(items.size=${items.size})" }
        _allItems.addAll(items)
        items.filter { searching.matches(it) }.forEach { item ->
            val index = findIndexFor(_items, item, sorting.selectedColumnValueExtractor)
            _items.add(index, item)
        }
        isAddingItems.set(false)
        if (triedToResetItems.get()) {
            triedToResetItems.set(false)
            log.debug { "Tried to reset items while adding some. Redoing now." }
            resetItems()
        }
    }

    open fun onItemsDeleted(items: List<ITEM>) {
        _allItems.removeAll(items)
        _items.removeAll(items)
    }

    open fun onItemSelected(item: SelectedItemType) {
        // fiddle around with venue being the item (special-case)
    }

    fun onVenueSelected(venue: Venue) {
        launchViewTask("Unable to select venue!") {
            log.trace { "Selected: $venue" }
            require(this::class == VenueViewModel::class) { "venue can only be clicked in VenueViewModel" }
            @Suppress("UNCHECKED_CAST")
            (selectedVenue as MutableStateFlow<Venue>).value = venue
            venueEdit.init(venue)
            onItemSelected(VenueSelected(venue))
            _selectedSubEntity.value = null
        }
    }

    fun onActivitySelected(activity: Activity) {
        launchViewTask("Unable to select activity!") {
            log.trace { "Selected: $activity" }
            _selectedSubEntity.value = SubEntity.ActivityEntity(activity)
            onItemSelected(ActivitySelected(activity))
        }
    }

    fun onFreetrainingSelected(freetraining: Freetraining) {
        launchViewTask("Unable to select freetraiing!") {
            log.trace { "Selected: $freetraining" }
            _selectedSubEntity.value = SubEntity.FreetrainingEntity(freetraining)
            onItemSelected(FreetrainingSelected(freetraining))
        }
    }

    fun onItemNavigation(navigation: TableNavigation, currentItem: ITEM) {
        log.trace { "onItemNavigation($navigation)" }
        _items.navigate(currentItem, navigation)?.also { newItem ->
            when (newItem) {
                is Activity -> onActivitySelected(newItem)
                is Freetraining -> onFreetrainingSelected(newItem)
                is Venue -> onVenueSelected(newItem)
                else -> error("Unhandled table item type: $newItem")
            }
        }
    }

    fun onSubActivityNavigated(navigation: TableNavigation, item: Activity) {
        log.trace { "onSubActivityNavigated($navigation, $item)" }
        selectedVenue.value!!.activities.navigate(item, navigation)?.also { newItem ->
            onActivitySelected(newItem)
        }
    }

    fun onSubFreetrainingNavigated(navigation: TableNavigation, item: Freetraining) {
        log.trace { "onSubFreetrainingNavigated($navigation, $item)" }
        selectedVenue.value!!.freetrainings.navigate(item, navigation)?.also { newItem ->
            onFreetrainingSelected(newItem)
        }
    }

    override fun onVenueUpdated(venue: Venue) {
        log.debug { "onVenueUpdated($venue)" }
        venueEdit.init(venue)
        resetItems() // not sure about the performance impact of this...
    }

    fun updateVenue() {
        venueEdit.updatePropertiesOf(selectedVenue.value!!)
        dataStorage.update(selectedVenue.value!!)
    }

    fun onBook(subEntity: SubEntity) {
        log.debug { "onBook: $subEntity" }
        when (val validity = validateBooking(subEntity)) {
            BookingValidation.Valid -> doBook(subEntity)
            is BookingValidation.Invalid -> {
                sharedModel.customDialog.value = CustomDialog(
                    title = "Booking",
                    text = validity.reason,
                    confirmLabel = "Book anyway",
                    onConfirm = {
                        doBook(subEntity)
                    },
                )
            }
        }
    }

    private fun validateBooking(subEntity: SubEntity): BookingValidation =
        when (subEntity) {
            is SubEntity.ActivityEntity -> bookingValidator.canBook(subEntity.activity)
            is SubEntity.FreetrainingEntity -> BookingValidation.Valid
        }

    private fun doBook(subEntity: SubEntity) {
        bookOrCancel(subEntity, BookingService::book) { result ->
            when (result) {
                BookingResult.BookingSuccess -> SnackbarEvent(
                    message = "Successfully ${subEntity.bookedLabel} '${subEntity.name}' ✅",
                )

                is BookingResult.BookingFail -> SnackbarEvent(
                    message = "Error while booking ❌\n${result.message}",
                    type = SnackbarType.Error,
                    duration = SnackbarDuration.Long,
                )
            }
        }
    }

    fun onCancelBooking(subEntity: SubEntity) {
        log.debug { "onCancelBooking: $subEntity" }
        when (val validity = validateCancelBooking(subEntity)) {
            BookingValidation.Valid -> doCancelBooking(subEntity)
            is BookingValidation.Invalid -> {
                sharedModel.customDialog.value = CustomDialog(
                    title = "Booking",
                    text = validity.reason,
                    confirmLabel = "Cancel anyway",
                    onConfirm = {
                        doCancelBooking(subEntity)
                    },
                )
            }
        }
    }

    private fun validateCancelBooking(subEntity: SubEntity): BookingValidation =
        when (subEntity) {
            is SubEntity.ActivityEntity -> bookingValidator.canCancel(subEntity.activity)
            is SubEntity.FreetrainingEntity -> BookingValidation.Valid
        }

    private fun doCancelBooking(subEntity: SubEntity) {
        log.debug { "doCancelBooking: $subEntity" }
        bookOrCancel(subEntity, BookingService::cancel) { result ->
            when (result) {
                CancelResult.CancelSuccess -> SnackbarEvent("Successfully cancelled booking for '${subEntity.name}' ✅")
                is CancelResult.CancelFail -> SnackbarEvent(
                    "Error while canceling ❌\n${result.message}",
                    SnackbarType.Error
                )
            }
        }
    }

    private fun <T> bookOrCancel(
        subEntity: SubEntity,
        bookingOperation: suspend BookingService.(SubEntity, Boolean, Boolean) -> T,
        resultHandler: (T) -> SnackbarEvent,
    ) {
        launchBackgroundTask(
            "Booking/Canceling activity/freetraining failed!",
            doBefore = {
                isBookingOrCancelInProgress = true
            },
            doFinally = {
                isBookingOrCancelInProgress = false
            },
        ) {
            val result = bookingService.bookingOperation(subEntity, isGcalEnabled, shouldGcalBeManaged.value)
            snackbarService.show(resultHandler(result))
        }
    }

    fun onActivityChangeToCheckedin(activity: Activity) {
        log.debug { "onActivityChangeToCheckedin($activity)" }
        bookingService.changeActivityToCheckedin(activity)
    }

    private fun resetItems() {
        if (isAddingItems.get()) {
            log.debug { "reset items was blocked by adding items. trying later again..." }
            triedToResetItems.set(true)
            return
        }
        log.trace { "resetItems for ${this::class.simpleName}" }
        _items.clear()
        _items.addAll(_allItems.filter { item ->
            searching.matches(item)
        }.let {
            sorting.sortIt(it)
        })
    }
}

sealed interface SelectedItemType
class VenueSelected(val venue: Venue) : SelectedItemType
class ActivitySelected(val activity: Activity) : SelectedItemType
class FreetrainingSelected(val freetraining: Freetraining) : SelectedItemType
