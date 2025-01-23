package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking
import seepick.localsportsclub.service.singles.SinglesService

interface PhpSessionProvider {
    fun provide(): PhpSessionId
}

object MockPhpSessionProvider : PhpSessionProvider {
    override fun provide(): PhpSessionId = PhpSessionId("mock")
}

class PhpSessionProviderImpl(
    private val singlesService: SinglesService,
    private val loginApi: LoginApi,
) : PhpSessionProvider {
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

    override fun provide(): PhpSessionId = cached
}
