package seepick.localsportsclub.sync

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.UscConfig

fun syncModule(uscConfig: UscConfig) = module {
    single { VenueSyncer(get(), get(), uscConfig.city, uscConfig.plan) }
    singleOf(::RealSyncerAdapter) bind Syncer::class
}
