package seepick.localsportsclub.view

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.AppPropertiesProvider
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.service.VersionChecker
import seepick.localsportsclub.service.VersionResult
import seepick.localsportsclub.view.common.executeBackgroundTask
import seepick.localsportsclub.view.common.executeViewTask
import java.net.URI

class VersionNotifier(
    private val versionChecker: VersionChecker,
    private val snackbarService: SnackbarService,
) : ViewModel(), ApplicationLifecycleListener {

    private val log = logger {}

    override fun onStartUp() {
        val version = AppPropertiesProvider.provide().version
        executeBackgroundTask("Unable to get version from the web.") {
            val result = versionChecker.check(version)
            when (result) {
                is VersionResult.TooOld -> {
                    executeViewTask("Failed to download newer version.") {
                        handleOutdated()
                    }
                }

                is VersionResult.UpToDate -> log.debug { "Current version [$version] is up2date." }
            }
        }
    }

    private fun handleOutdated() {
        snackbarService.show(
            message = "Your current version is out-of-date. Please download a newer one.",
            actionLabel = "Download",
            duration = SnackbarDuration.Long,
            onResult = {
                when (it) {
                    SnackbarResult.Dismissed -> log.debug { "Download newer version dismissed." }
                    SnackbarResult.ActionPerformed -> {
                        val url =
                            "https://github.com/seepick/LocalSportsClub/releases/latest/download/LocalSportsClub." +
                                    (if (System.getProperty("os.name") == "Mac OS X") "dmg" else "exe")
                        java.awt.Desktop.getDesktop().browse(URI(url))
                    }
                }
            }
        )
    }
}
