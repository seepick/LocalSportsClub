package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.SyncMode
import seepick.localsportsclub.sync.thirdparty.HotFlowYogaFetcher
import seepick.localsportsclub.sync.thirdparty.MovementsYogaFetcher
import seepick.localsportsclub.sync.thirdparty.ThirdPartySyncer

private val log = logger {}

fun syncModule(config: AppConfig) = module {
    log.debug { "Configuring sync mode: ${config.sync}" }
    singleOf(::SyncerListenerDispatcher)
    singleOf(::HttpDownloader) bind Downloader::class
    when (config.sync) {
        SyncMode.Noop -> single { NoopSyncer } bind Syncer::class
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
            singleOf(::HotFlowYogaFetcher)
            singleOf(::ThirdPartySyncer)
            singleOf(::CleanupSyncer)
            singleOf(::SyncerFacade) bind Syncer::class

        }
    }
}
