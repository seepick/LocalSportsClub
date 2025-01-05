package seepick.localsportsclub.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    fun startSync() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                log.info { "startSync()" }
                isSyncing = true
                syncer.sync()
                log.info { "startSync() DONE" }
                isSyncing = false
            }
        }
    }

    fun select(screen: Screen) {
        log.debug { "selected $screen" }
        selectedScreen.value = screen
    }

    override fun onKeyboardChangeScreen(screenNr: Int) {
        select(Screen.entries.first { it.ordinal == (screenNr - 1) })
    }
}
