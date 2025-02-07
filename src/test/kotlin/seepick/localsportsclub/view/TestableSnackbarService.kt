package seepick.localsportsclub.view

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class TestableSnackbarService() : SnackbarService {

    val sentEvents = mutableListOf<SnackbarEvent>()
    override val events: Flow<SnackbarEvent> = MutableSharedFlow()

    override suspend fun show(
        event: SnackbarEvent,
    ) {
        sentEvents += event
    }
}
