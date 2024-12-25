package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.api.activities.ActivityApi
import seepick.localsportsclub.api.activities.ActivityHttpApi
import seepick.localsportsclub.api.schedule.ScheduleApi
import seepick.localsportsclub.api.schedule.ScheduleHttpApi
import seepick.localsportsclub.api.venue.VenueApi
import seepick.localsportsclub.api.venue.VenueHttpApi
import seepick.localsportsclub.service.httpClient

private val log = logger {}

@JvmInline
value class PhpSessionId(val value: String) {
    override fun toString() = value
}

fun apiModule(config: AppConfig) = module {
    if (config.api == AppConfig.ApiMode.Mock) {
        log.debug { "Wiring mocked USC API." }
        singleOf(::MockUscApi) bind UscApi::class

    } else if (config.api == AppConfig.ApiMode.RealHttp) {
        val phpSessionId = runBlocking {
            val result = LoginApi(httpClient, config.usc.baseUrl).login(Credentials.load())
            require(result is LoginResult.Success) { "Login failed: $result" }
            result.phpSessionId
        }
        single { if (config.usc.storeResponses) ResponseStorageImpl() else NoopResponseStorage } bind ResponseStorage::class
        single { PhpSessionId(phpSessionId) } bind PhpSessionId::class
        singleOf(::VenueHttpApi) bind VenueApi::class
        singleOf(::ActivityHttpApi) bind ActivityApi::class
        singleOf(::ScheduleHttpApi) bind ScheduleApi::class
        singleOf(::UscApiAdapter) bind UscApi::class
    }
}
