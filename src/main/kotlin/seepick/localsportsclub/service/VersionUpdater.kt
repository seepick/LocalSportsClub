package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.awt.Desktop
import java.io.File
import java.net.URI

object VersionUpdater {

    private val log = logger {}
    private val latestDownloadUrl =
        "https://github.com/seepick/LocalSportsClub/releases/latest/download/LocalSportsClub.${OsSniffer.os.installerSuffix}"

    fun downloadLatestVersion() {
        if (OsSniffer.os == OsSniffer.Os.mac) {
            log.debug { "Auto-updating application for macOS by using update script." }
            autoUpdate()
            log.info { "Script started. It will request an application quit..." }
        } else {
            log.debug { "Downloading latest version from: $latestDownloadUrl" }
            Desktop.getDesktop().browse(URI(latestDownloadUrl))
        }
    }

    private fun autoUpdate() {
        val script = copyScript()
        executeScriptInBg(script.absolutePath)
    }

    private fun copyScript(): File {
        val input = openFromClasspath("/update.scpt")
        val target = File.createTempFile("update", ".scpt")
        target.writeBytes(input.readAllBytes())
        return target
    }

    private fun executeScriptInBg(scriptLocation: String) {
        log.info { "Executing applescript located at: $scriptLocation" }
        ProcessBuilder("sh", "-c", "nohup osascript $scriptLocation &")
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
    }
}
