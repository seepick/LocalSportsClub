package seepick.localsportsclub.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.GlobalKeyboardListener
import seepick.localsportsclub.sync.Syncer

class MainViewModel(
    private val syncer: Syncer,
    firstScreen: Screen?,
) : ViewModel(), GlobalKeyboardListener {

    private val log = logger {}

    var selectedScreen = mutableStateOf(firstScreen ?: Screen.Activities)
        private set
    var isSyncing: Boolean by mutableStateOf(false)
        private set

    suspend fun startSync() {
        log.info { "startSync()" }
        isSyncing = true
        syncer.sync()
        log.info { "startSync() DONE" }
        isSyncing = false
    }

    fun select(screen: Screen) {
        log.debug { "selected $screen" }
        selectedScreen.value = screen
    }

    override fun onKeyboardChangeScreen(screenNr: Int) {
        select(Screen.entries.single { it.ordinal == (screenNr - 1) })
    }
}
