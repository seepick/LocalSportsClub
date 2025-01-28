package seepick.localsportsclub.view

import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.GlobalKeyboardListener
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.sync.SyncProgressListener
import seepick.localsportsclub.sync.SyncReporter
import seepick.localsportsclub.sync.SyncStep
import seepick.localsportsclub.sync.Syncer
import seepick.localsportsclub.view.common.executeBackgroundTask

class MainViewModel(
    private val syncer: Syncer,
    private val singlesService: SinglesService,
    private val snackbarService: SnackbarService,
    private val syncReporter: SyncReporter,
) : ViewModel(), GlobalKeyboardListener, ApplicationLifecycleListener, SyncProgressListener {

    private val log = logger {}

    var selectedScreen = mutableStateOf(Screen.Activities)
        private set
    var isSyncing: Boolean by mutableStateOf(false)
        private set
    var isSyncPossible: Boolean by mutableStateOf(false)
        private set
    var currentSyncStep by mutableStateOf<SyncStep?>(null)
        private set
    private var currentSyncJob: Job? = null
    private var currentSyncJobCancelled = false

    override fun onStartUp() {
        log.debug { "onStartUp()" }
        val preferences = singlesService.preferences
        isSyncPossible = preferences.uscCredentials != null && preferences.city != null
    }

    override fun onExit() {
        log.debug { "onExit()" }
        runBlocking {
            log.debug { "Cancel and join sync job ..." }
            currentSyncJobCancelled = true
            currentSyncJob?.cancelAndJoin()
            log.debug { "Cancel and join sync job ... DONE" }
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

    override fun onSyncStart() {
        log.info { "sync START" }
        isSyncing = true
        currentSyncJobCancelled = false
    }

    fun startSync() {
        currentSyncJob = executeBackgroundTask("Synchronisation of data failed!") {
            currentSyncStep = null
            syncer.sync()
        }
    }

    override fun onSyncStep(syncStep: SyncStep) {
        currentSyncStep = syncStep
    }

    override fun onSyncFinish() {
        log.info { "sync DONE" }
        isSyncing = false
        currentSyncJob = null

        if (!currentSyncJobCancelled) {
            val report = syncReporter.report.buildMessage()
            syncReporter.clear()
            snackbarService.show(
                message = buildString {
                    append("Finished synchronizing data 🔄✅")
                    if (report != null) {
                        appendLine()
                        append(report)
                    }
                },
                duration = SnackbarDuration.Indefinite,
                actionLabel = "Close",
            )
        }
    }
}
