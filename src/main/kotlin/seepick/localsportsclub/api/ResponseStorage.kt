package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.SystemClock
import java.io.File
import java.io.FilenameFilter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface ResponseStorage {
    suspend fun store(response: HttpResponse, suffix: String)
}

object NoopResponseStorage : ResponseStorage {
    override suspend fun store(response: HttpResponse, suffix: String) {
        // noop
    }
}

class ResponseStorageImpl : ResponseStorage {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ResponseStorageImpl().cleanUp()
        }
    }

    private val log = logger {}
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS")
    private var cleanedUpYet = false
    private val apiLogsFolder = FileResolver.resolve(DirectoryEntry.ApiLogs)

    override suspend fun store(response: HttpResponse, suffix: String) {
        if (!cleanedUpYet) {
            cleanUp()
            cleanedUpYet = true
        }
        val target = File(apiLogsFolder, "${dateTimeFormatter.format(LocalDateTime.now())}-$suffix.apilog.txt")
        target.writeText(response.bodyAsText())
    }

    private fun cleanUp() {
        val yesterday = SystemClock.now().minusDays(1)
        val filter = FilenameFilter { _, name ->
            if (!name.endsWith(".apilog.txt")) {
                false
            } else {
                val date = LocalDateTime.from(dateTimeFormatter.parse(name.substringBefore("-")))
                date.isBefore(yesterday)
            }
        }
        val oldApiLogFiles = apiLogsFolder.listFiles(filter)!!
        log.debug { "Going to delete ${oldApiLogFiles.size} old response log files." }
        oldApiLogFiles.forEach {
            it.delete()
        }
    }
}
