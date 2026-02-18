package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.LscConfig
import seepick.localsportsclub.sync.domain.ActivitiesSyncer
import seepick.localsportsclub.sync.domain.CheckinSyncer
import seepick.localsportsclub.sync.domain.CleanupPostSync
import seepick.localsportsclub.sync.domain.DataSyncRescuer
import seepick.localsportsclub.sync.domain.DataSyncRescuerImpl
import seepick.localsportsclub.sync.domain.FreetrainingSyncer
import seepick.localsportsclub.sync.domain.ScheduleSyncer
import seepick.localsportsclub.sync.domain.SyncerFacade
import seepick.localsportsclub.sync.domain.VenueAutoSyncer
import seepick.localsportsclub.sync.domain.VenueSyncInserter
import seepick.localsportsclub.sync.domain.VenueSyncInserterImpl
import seepick.localsportsclub.sync.domain.VenueSyncer

private val log = logger {}

enum class SyncMode {
    Noop, Delayed, Dummy, Real
}

fun syncModule(config: LscConfig) = module {
    log.debug { "Configuring sync mode: ${config.sync}" }
    singleOf(::SyncerListenerDispatcher)
    singleOf(::SyncReporter)
    singleOf(::SyncProgressThreaded) bind SyncProgress::class
    singleOf(::HttpDownloader) bind Downloader::class

    when (config.sync) {
        SyncMode.Noop -> singleOf(::NoopSyncer) bind Syncer::class
        SyncMode.Delayed -> singleOf(::SyncerDelayed) bind Syncer::class
        SyncMode.Dummy -> singleOf(::SyncerDummy) bind Syncer::class
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
