package seepick.localsportsclub

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.api.UscConfig
import seepick.localsportsclub.api.apiModule
import seepick.localsportsclub.gcal.gcalModule
import seepick.localsportsclub.persistence.persistenceModule
import seepick.localsportsclub.service.serviceModule
import seepick.localsportsclub.sync.syncModule
import seepick.localsportsclub.view.viewModule

fun allModules(config: AppConfig) = listOf(
    rootModule(config),
    persistenceModule(config),
    apiModule(config),
    serviceModule(config),
    gcalModule(config),
    syncModule(config),
    viewModule(),
)

fun rootModule(config: AppConfig): Module = module {
    single { config.usc } bind UscConfig::class
    singleOf(::GlobalKeyboard)
    singleOf(::ApplicationLifecycle)
    singleOf(::MainWindowState)
    // or use includeModules() ;)
}

// not working for compose ViewModels :(
//@OptIn(KoinInternalApi::class)
//inline fun <reified T : Any> getAllCustom(): List<T> =
//    getKoin().let { koin ->
//        koin.instanceRegistry.instances.map { it.value.beanDefinition }
//            .filter { it.kind == Kind.Singleton }
//            .filter { it.primaryType.isSubclassOf(T::class) }
//            .map { koin.get(clazz = it.primaryType, qualifier = null, parameters = null) }
//    }
