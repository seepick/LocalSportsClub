package seepick.localsportsclub.service

import com.github.seepick.uscclient.shared.safeGet
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import java.net.URL

interface VersionChecker {
    suspend fun check(currentVersion: String): VersionResult
}

sealed interface VersionResult {
    companion object {
        const val SNAPSHOT_VERSION = "1.0.0"
    }

    data class UpToDate(val currentVersion: String) : VersionResult
    data class TooOld(val currentVersion: String, val latestVersion: String) : VersionResult
}

class OnlineVersionChecker(
    private val httpClient: HttpClient,
) : VersionChecker {

    private val log = logger {}
    private val versionFileUrl = URL("https://raw.githubusercontent.com/seepick/LocalSportsClub/main/version.txt")

    override suspend fun check(currentVersion: String): VersionResult {
        if (currentVersion == VersionResult.SNAPSHOT_VERSION) {
            log.debug { "Current version is snapshot developer version; skip online check." }
            return VersionResult.UpToDate(currentVersion)
        }
        log.debug { "Checking version for $currentVersion ..." }
        val onlineVersion = loadOnlineVersion()
        log.debug { "Online version is: $onlineVersion" }
        return if (onlineVersion != currentVersion) {
            VersionResult.TooOld(currentVersion = currentVersion, latestVersion = onlineVersion)
        } else {
            VersionResult.UpToDate(currentVersion)
        }
    }

    private suspend fun loadOnlineVersion(): String {
        val response = httpClient.safeGet(versionFileUrl)
        val responseText = response.bodyAsText()
        return responseText.trim()
    }
}

object NoopVersionChecker : VersionChecker {
    override suspend fun check(currentVersion: String) = VersionResult.UpToDate(currentVersion)
}
