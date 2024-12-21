package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.Environment
import java.io.File

private val log = logger {}

object FileResolver {

    private val homeDirectory = File(System.getProperty("user.home"))
    private val appDirectoryProd = File(homeDirectory, ".lsc")
    private val appDirectoryDev = File(homeDirectory, ".lsc-dev")
    private val appDirectory = when (Environment.current) {
        Environment.Production -> appDirectoryProd
        Environment.Development -> appDirectoryDev
    }

    init {
        appDirectory.createIfNeededOrFail()
    }

    fun resolve(entry: FileEntry) = File(appDirectory, entry.fileName)
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

enum class FileEntry(val fileName: String) {
    Login("login.json"),
}

enum class DirectoryEntry(val directoryName: String) {
    Database("database"),

    //    JsonLogs("json_logs"),
//    ApplicationLogs("app_logs"),
    VenueImages("venueImages"),
}
