package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.io.File

private val log = logger {}

interface FileResolver {
    fun resolve(entry: FileEntry): File
    fun resolve(entry: DirectoryEntry): File
}

class FileResolverImpl(private val appDirectory: File) : FileResolver {
    init {
        appDirectory.createIfNeededOrFail()
    }

    override fun resolve(entry: FileEntry) =
        File(if (entry.directory == null) appDirectory else resolve(entry.directory), entry.fileName)

    override fun resolve(entry: DirectoryEntry) =
        File(appDirectory, entry.directoryName).createIfNeededOrFail()
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
    GoogleCredentials(DirectoryEntry.Gcal, "credentials.json"),
    GoogleCredentialsCache(DirectoryEntry.Gcal, "StoredCredential"),
    GoogleCalendarCredsProperties(DirectoryEntry.Gcal, "credentials.properties"),
}

enum class DirectoryEntry(val directoryName: String) {
    Database("database"),
    ApiLogs("api_logs"),
    VenueImages("venueImages"),
    Logs("logs"),
    Gcal("google_calendar"),
}
