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
import seepick.localsportsclub.service.ApplicationLifecycleListener
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.sync.SyncProgressListener
import seepick.localsportsclub.sync.SyncReporter
import seepick.localsportsclub.sync.SyncStep
import seepick.localsportsclub.sync.Syncer
import seepick.localsportsclub.view.common.launchBackgroundTask
import seepick.localsportsclub.view.common.launchViewTask
import seepick.localsportsclub.view.shared.SharedModel
import java.time.LocalDate

class MainViewModel(
    private val syncer: Syncer,
    private val singlesService: SinglesService,
    private val snackbarService: SnackbarService,
    private val syncReporter: SyncReporter,
    private val sharedModel: SharedModel,
    private val fileResolver: FileResolver,
) : ViewModel(), GlobalKeyboardListener, ApplicationLifecycleListener, SyncProgressListener {

    private val log = logger {}

    var selectedScreen = mutableStateOf(Screen.Activities)
        private set
    var isSyncInProgress: Boolean by mutableStateOf(false)
        private set
    var isSyncPossible: Boolean by mutableStateOf(false)
        private set
    var currentSyncStep by mutableStateOf<SyncStep?>(null)
        private set
    private var currentSyncJob: Job? = null
    private var currentSyncJobCancelled = false

    var lastSync by mutableStateOf("N/A")
        private set

    override fun onStartUp() {
        log.debug { "onStartUp()" }
        val preferences = singlesService.preferences
        sharedModel.isUscConnectionVerified.value = singlesService.verifiedUscCredentials != null
        sharedModel.verifiedUscUsername.value = singlesService.verifiedUscCredentials?.username
        sharedModel.verifiedUscPassword.value = singlesService.verifiedUscCredentials?.password
        isSyncPossible = sharedModel.isUscConnectionVerified.value && preferences.city != null
        sharedModel.verifiedGcalId.value = singlesService.verifiedGcalId
        lastSync = calcLastSync()
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

    override fun onKeyboardChangeScreen(screen: Screen) {
        changeScreen(screen)
    }

    fun changeScreen(screen: Screen) {
        log.debug { "selecting: $screen" }
        selectedScreen.value = screen
    }

    override fun onSyncStart() {
        log.info { "sync START" }
        isSyncInProgress = true
        currentSyncJobCancelled = false
    }

    fun startSync() {
        syncReporter.clear()
        currentSyncJob = launchBackgroundTask("Synchronisation of data failed!", fileResolver) {
            currentSyncStep = null
            syncer.sync()
        }
    }

    override fun onSyncStep(syncStep: SyncStep) {
        currentSyncStep = syncStep
    }

    override fun onSyncFinish(isError: Boolean) {
        log.info { "onSyncFinish(isError=$isError)" }
        isSyncInProgress = false
        currentSyncJob = null
        lastSync = calcLastSync()

        if (!currentSyncJobCancelled) {
            launchViewTask("Failed to show snackbar!", fileResolver) {
                snackbarService.show(
                    SnackbarEvent(
                        content = if (isError) SnackbarContent.TextContent("See error dialog for details...") else SnackbarContent.CustomContent(
                            syncReporter.report.buildContent()
                        ),
                        duration = SnackbarDuration.Indefinite,
                        actionLabel = "Close",
                        type = if (isError) SnackbarType.Error else SnackbarType.Info
                    )
                )
            }
        }
    }

    private fun calcLastSync(): String {
        val city = singlesService.preferences.city ?: return "N/A"
        val last = singlesService.getLastSyncFor(city) ?: return "N/A"
        return last.prettyPrint(LocalDate.now().year)
    }
}
