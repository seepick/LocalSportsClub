package seepick.localsportsclub

import seepick.localsportsclub.api.apiModule
import seepick.localsportsclub.persistence.persistenceModule
import seepick.localsportsclub.service.serviceModule
import seepick.localsportsclub.sync.syncModule
import seepick.localsportsclub.view.viewModule

fun allModules(config: AppConfig) = listOf(
    persistenceModule(config),
    apiModule(config),
    serviceModule(),
    syncModule(config),
    viewModule(),
)
