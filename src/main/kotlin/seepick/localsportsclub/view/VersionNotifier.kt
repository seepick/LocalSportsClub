package seepick.localsportsclub.view

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import seepick.localsportsclub.service.AppPropertiesProvider
import seepick.localsportsclub.service.ApplicationLifecycleListener
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.VersionChecker
import seepick.localsportsclub.service.VersionResult
import seepick.localsportsclub.service.VersionUpdater
import seepick.localsportsclub.view.common.launchBackgroundTask
import java.net.UnknownHostException

class VersionNotifier(
    private val versionChecker: VersionChecker,
    private val snackbarService: SnackbarService,
    private val fileResolver: FileResolver,
) : ViewModel(), ApplicationLifecycleListener {

    private val log = logger {}

    override fun onStartUp() {
        val version = AppPropertiesProvider.provide().version
        launchBackgroundTask("Failed to get latest application version from the web.", fileResolver) {
            try {
                val result = versionChecker.check(version)
                when (result) {
                    is VersionResult.TooOld -> {
                        withContext(Dispatchers.Main) {
                            handleOutdated()
                        }
                    }

                    is VersionResult.UpToDate -> log.debug { "Current version [$version] is up2date." }
                }
            } catch (_: UnknownHostException) {
                log.debug { "Skip version check as seems not to be connected to the internet." }
            }
        }
    }

    private suspend fun handleOutdated() {
        snackbarService.show(
            message = "Your current version is out-of-date 👴🏻 Please download a newer one 🙏🏻",
            actionLabel = "Download",
            duration = SnackbarDuration.Long,
            onResult = {
                when (it) {
                    SnackbarResult.Dismissed -> log.debug { "Download newer version dismissed." }
                    SnackbarResult.ActionPerformed -> VersionUpdater.downloadLatestVersion()
                }
            }
        )
    }
}
