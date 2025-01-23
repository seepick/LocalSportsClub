package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.Environment
import java.io.File

private val log = logger {}

object FileResolver {

    private val homeDirectory = File(System.getProperty("user.home"))
    val appDirectoryProd = File(homeDirectory, ".lsc")
    val appDirectoryDev = File(homeDirectory, ".lsc-dev")
    private val appDirectory =
        when (Environment.current) {
            Environment.Production -> appDirectoryProd
            Environment.Development -> appDirectoryDev
        }

    init {
        appDirectory.createIfNeededOrFail()
    }

    fun resolve(entry: FileEntry) =
        File(if (entry.directory == null) appDirectory else resolve(entry.directory), entry.fileName)

    fun resolve(entry: DirectoryEntry) = File(appDirectory, entry.directoryName).createIfNeededOrFail()
}

private fun File.createIfNeededOrFail() = apply {
    if (!exists()) {
        log.debug { "Creating directory at: $absolutePath" }
        if (!mkdirs()) {
            error("Could not create directory at: $absolutePath")
        }
    }
}

enum class FileEntry(val directory: DirectoryEntry?, val fileName: String) {
    GcalCredentials(DirectoryEntry.Gcal, "credentials.json"),
}

enum class DirectoryEntry(val directoryName: String) {
    Database("database"),
    ApiLogs("api_logs"),
    VenueImages("venueImages"),
    Logs("logs"),
    Gcal("google_calendar"),
}
