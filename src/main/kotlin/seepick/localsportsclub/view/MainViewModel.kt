package seepick.localsportsclub.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.sync.Syncer

class MainViewModel(
    private val syncer: Syncer,
) : ViewModel() {

    private val log = logger {}
    var isSyncing: Boolean by mutableStateOf(false)
        private set

    suspend fun startSync() {
        log.info { "startSync()" }
        isSyncing = true
        syncer.sync()
        log.info { "startSync() DONE" }
        isSyncing = false
    }
}
