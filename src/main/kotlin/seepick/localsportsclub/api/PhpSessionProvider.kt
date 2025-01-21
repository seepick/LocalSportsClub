package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking
import seepick.localsportsclub.service.SinglesService

class PhpSessionProvider(
    private val singlesService: SinglesService,
    private val loginApi: LoginApi,
) {
    private val log = logger {}

    private val cached: PhpSessionId by lazy {
        runBlocking {
            log.info { "Creating cached PHP Session ID..." }
            val creds = singlesService.preferences.uscCredentials ?: error("No USC credentials set!")
            val result = loginApi.login(creds)
            require(result is LoginResult.Success) { "Login failed: $result" }
            result.phpSessionId
        }
    }

    fun provide(): PhpSessionId = cached
}
