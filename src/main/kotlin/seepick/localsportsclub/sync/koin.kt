package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig

private val log = logger {}

fun syncModule(config: AppConfig) = module {
    log.debug { "Configuring sync mode: ${config.sync}" }
    singleOf(::SyncerListenerDispatcher) bind SyncerListenerDispatcher::class
    singleOf(::HttpDownloader) bind Downloader::class
    when (config.sync) {
        AppConfig.SyncMode.Noop -> single { NoopSyncer } bind Syncer::class
        AppConfig.SyncMode.Delayed -> singleOf(::DelayedSyncer) bind Syncer::class
        AppConfig.SyncMode.Real -> {
            singleOf(::VenueSyncer) bind VenueSyncer::class
            singleOf(::ActivitiesSyncer) bind ActivitiesSyncer::class
            singleOf(::ScheduleSyncer) bind ScheduleSyncer::class
            singleOf(::SyncerFacade) bind Syncer::class
        }
    }
}
