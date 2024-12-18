package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.api.venue.VenueApi
import seepick.localsportsclub.api.venue.VenueHttpApi
import seepick.localsportsclub.service.httpClient

private val log = logger {}

fun apiModule(config: AppConfig) = module {
    if (config.api == AppConfig.ApiMode.Mock) {
        log.debug { "Wiring mocked USC API." }
        singleOf(::MockUscApi) bind UscApi::class

    } else if (config.api == AppConfig.ApiMode.Real) {
        val phpSessionId = runBlocking {
            val result = LoginApi(httpClient, config.usc.baseUrl).login(Credentials.load())
            require(result is LoginResult.Success) { "Login failed! ${result}" }
            result.phpSessionId
        }
        single {
            VenueHttpApi(
                http = httpClient,
                baseUrl = config.usc.baseUrl,
                phpSessionId = phpSessionId
            )
        } bind VenueApi::class
        single { UscApiAdapter(get()) } bind UscApi::class
    }
}
