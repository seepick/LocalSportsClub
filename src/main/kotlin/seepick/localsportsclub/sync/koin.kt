package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.LscConfig
import seepick.localsportsclub.SyncMode

private val log = logger {}

fun syncModule(config: LscConfig) = module {
    log.debug { "Configuring sync mode: ${config.sync}" }
    singleOf(::SyncerListenerDispatcher)
    singleOf(::SyncReporter)
    singleOf(::SyncProgressThreaded) bind SyncProgress::class
    singleOf(::HttpDownloader) bind Downloader::class

    when (config.sync) {
        SyncMode.Noop -> singleOf(::NoopSyncer) bind Syncer::class
        SyncMode.Delayed -> singleOf(::DelayedSyncer) bind Syncer::class
        SyncMode.Dummy -> singleOf(::DummySyncer) bind Syncer::class
        SyncMode.Real -> {
            singleOf(::DataSyncRescuerImpl) bind DataSyncRescuer::class
            singleOf(::VenueSyncInserterImpl) bind VenueSyncInserter::class
            singleOf(::VenueSyncer)
            singleOf(::ActivitiesSyncer)
            singleOf(::FreetrainingSyncer)
            singleOf(::ScheduleSyncer)
            singleOf(::CheckinSyncer)
            singleOf(::CleanupPostSync)
            single {
                SyncerFacade(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    syncDaysAhead = config.syncDaysAhead,
                )
            } bind Syncer::class
            singleOf(::VenueAutoSyncer)
        }
    }
}
