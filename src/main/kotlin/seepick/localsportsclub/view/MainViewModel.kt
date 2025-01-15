package seepick.localsportsclub.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.GlobalKeyboardListener
import seepick.localsportsclub.sync.Syncer
import seepick.localsportsclub.view.common.executeBackgroundTask

class MainViewModel(
    private val syncer: Syncer,
    firstScreen: Screen?,
) : ViewModel(), GlobalKeyboardListener {

    private val log = logger {}

    var selectedScreen = mutableStateOf(firstScreen ?: Screen.Activities)
        private set
    var isSyncing: Boolean by mutableStateOf(false)
        private set

    fun startSync() {
        executeBackgroundTask(
            "Synchronisation of data failed!",
            doBefore = {
                log.info { "startSync()" }
                isSyncing = true
            },
            doFinally = {
                log.info { "startSync() DONE" }
                isSyncing = false
            }
        ) {
            syncer.sync()
        }
    }

    override fun onKeyboardChangeScreen(screenNr: Int) {
        log.debug { "selecting screen nr: $screenNr" }
        val screen = Screen.entries.first { it.ordinal == (screenNr - 1) }
        selectedScreen.value = screen
    }
}
