package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Desktop
import java.io.File
import java.net.URI

object VersionUpdater {

    private val log = KotlinLogging.logger {}
    private val latestDownloadUrl =
        "https://github.com/seepick/LocalSportsClub/releases/latest/download/LocalSportsClub.${OsSniffer.os.installerSuffix}"
    private val scriptLocation = "/Applications/LocalSportsClub.app/Contents/Resources/update.scpt"

    fun downloadLatestVersion() {
        if (OsSniffer.os == OsSniffer.Os.mac && File(scriptLocation).exists()) {
            autoUpdate()
        } else {
            log.debug { "Downloading latest version at: $latestDownloadUrl" }
            Desktop.getDesktop().browse(URI(latestDownloadUrl))
        }
    }

    private fun autoUpdate() {
        log.info { "Executing shell script located at: $scriptLocation" }
        ProcessBuilder("sh", "-c", "nohup osascript $scriptLocation &")
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
    }
}
