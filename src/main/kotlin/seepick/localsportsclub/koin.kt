package seepick.localsportsclub

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.api.apiModule
import seepick.localsportsclub.persistence.persistenceModule
import seepick.localsportsclub.service.serviceModule
import seepick.localsportsclub.sync.syncModule
import seepick.localsportsclub.view.viewModule

fun allModules(config: AppConfig) = listOf(
    rootModule(config),
    persistenceModule(config),
    apiModule(config),
    serviceModule(),
    syncModule(config),
    viewModule(),
)

fun rootModule(config: AppConfig): Module = module {
    single { config.usc } bind UscConfig::class
}
