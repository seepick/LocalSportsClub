package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig

private val log = logger {}

fun syncModule(config: AppConfig) = module {
    single { VenueSyncer(get(), get(), get(), config.usc.city, config.usc.plan) }
    log.debug { "Configuring sync mode: ${config.sync}" }
    when (config.sync) {
        AppConfig.SyncMode.Noop -> single { NoopSyncer } bind Syncer::class
        AppConfig.SyncMode.Delayed -> singleOf(::DelayedSyncer) bind Syncer::class
        AppConfig.SyncMode.Real -> singleOf(::RealSyncerAdapter) bind Syncer::class
    }
    singleOf(::SyncDispatcher) bind SyncDispatcher::class
}
