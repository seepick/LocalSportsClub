package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.service.httpClient

private val log = logger {}

fun syncModule(config: AppConfig) = module {
    single {
        VenueSyncer(
            api = get(),
            venueRepo = get(),
            syncDispatcher = get(),
            venueLinksRepo = get(),
            downloader = get(),
            imageStorage = get(),
            city = config.usc.city,
            plan = config.usc.plan,
        )
    }
    single {
        ActivitiesSyncer(
            api = get(),
            syncDispatcher = get(),
            activityRepo = get(),
            venueRepo = get(),
            city = config.usc.city,
            plan = config.usc.plan,
            clock = get(),
        )
    }
    singleOf(::ScheduleSyncer)
    log.debug { "Configuring sync mode: ${config.sync}" }
    when (config.sync) {
        AppConfig.SyncMode.Noop -> single { NoopSyncer } bind Syncer::class
        AppConfig.SyncMode.Delayed -> singleOf(::DelayedSyncer) bind Syncer::class
        AppConfig.SyncMode.Real -> singleOf(::SyncerFacade) bind Syncer::class
    }
    singleOf(::SyncDispatcher) bind SyncDispatcher::class
    single { HttpDownloader(httpClient) } bind Downloader::class
}
