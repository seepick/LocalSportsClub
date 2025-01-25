package seepick.localsportsclub.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.GlobalKeyboardListener
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.sync.Syncer
import seepick.localsportsclub.view.common.executeBackgroundTask

class MainViewModel(
    private val syncer: Syncer,
    private val singlesService: SinglesService,
    private val snackbarService: SnackbarService,
) : ViewModel(), GlobalKeyboardListener, ApplicationLifecycleListener {

    private val log = logger {}

    var selectedScreen = mutableStateOf(Screen.Activities)
        private set
    var isSyncing: Boolean by mutableStateOf(false)
        private set
    var isSyncPossible: Boolean by mutableStateOf(false)
        private set

    override fun onStartUp() {
        val preferences = singlesService.preferences
        isSyncPossible = preferences.uscCredentials != null && preferences.city != null
    }

    fun startSync() {
        executeBackgroundTask(
            "Synchronisation of data failed!",
            doBefore = {
                log.info { "sync START" }
                isSyncing = true
            },
            doFinally = {
                log.info { "sync DONE" }
                isSyncing = false
            }
        ) {
            syncer.sync()
            snackbarService.show("Finished synchronizing data ✅")
        }
    }

    override fun onKeyboardChangeScreen(screenNr: Int) {
        val screen = Screen.entries.first { it.ordinal == (screenNr - 1) }
        changeScreen(screen)
    }

    fun changeScreen(screen: Screen) {
        log.debug { "selecting: $screen" }
        selectedScreen.value = screen
    }
}
