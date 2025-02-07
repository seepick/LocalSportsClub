package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.SyncMode
import seepick.localsportsclub.sync.thirdparty.DeNieuweYogaSchoolFetcher
import seepick.localsportsclub.sync.thirdparty.EversportsFetcher
import seepick.localsportsclub.sync.thirdparty.MovementsYogaFetcher
import seepick.localsportsclub.sync.thirdparty.ThirdPartySyncerAmsterdam

private val log = logger {}

fun syncModule(config: AppConfig) = module {
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
            singleOf(::MovementsYogaFetcher)
            singleOf(::EversportsFetcher)
            singleOf(::DeNieuweYogaSchoolFetcher)
            singleOf(::ThirdPartySyncerAmsterdam)
            singleOf(::CleanupPostSync)
            singleOf(::SyncerFacade) bind Syncer::class
        }
    }
}
