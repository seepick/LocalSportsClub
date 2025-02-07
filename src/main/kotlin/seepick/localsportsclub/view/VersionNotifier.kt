package seepick.localsportsclub.view

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import seepick.localsportsclub.AppPropertiesProvider
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.service.VersionChecker
import seepick.localsportsclub.service.VersionResult
import seepick.localsportsclub.view.common.launchBackgroundTask
import java.net.URI
import java.net.UnknownHostException

class VersionNotifier(
    private val versionChecker: VersionChecker,
    private val snackbarService: SnackbarService,
) : ViewModel(), ApplicationLifecycleListener {

    private val log = logger {}

    override fun onStartUp() {
        val version = AppPropertiesProvider.provide().version
        launchBackgroundTask("Failed to get latest application version from the web.") {
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
            } catch (e: UnknownHostException) {
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
                    SnackbarResult.ActionPerformed -> downloadLatestVersion()
                }
            }
        )
    }

    private fun downloadLatestVersion() {
        val url = "https://github.com/seepick/LocalSportsClub/releases/latest/download/LocalSportsClub." +
                (if (System.getProperty("os.name") == "Mac OS X") "dmg" else "exe")
        log.debug { "Downloading latest version at: $url" }
        java.awt.Desktop.getDesktop().browse(URI(url))
    }
}
