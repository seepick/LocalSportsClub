package seepick.localsportsclub

import seepick.localsportsclub.api.apiModule
import seepick.localsportsclub.persistence.persistenceModule
import seepick.localsportsclub.service.logicModule
import seepick.localsportsclub.sync.syncModule

fun allModules(config: AppConfig) = listOf(
    persistenceModule(config),
    apiModule(config),
    logicModule(),
    syncModule(config.usc),
)
