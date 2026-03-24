package seepick.localsportsclub

import org.koin.core.annotation.KoinInternalApi
import org.koin.core.definition.Kind
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent
import seepick.localsportsclub.service.serviceModule
import seepick.localsportsclub.sync.syncInfraModule
import seepick.localsportsclub.view.viewModule
import kotlin.reflect.full.isSubclassOf

fun allModules(
    config: LscConfig,
    persistenceModule: Module,
    gcalModule: Module,
    uscClientModule: Module,
    syncModule: Module,
) = listOf(
    rootModule(),
    persistenceModule,
    uscClientModule,
    serviceModule(config),
    gcalModule,
    syncInfraModule(config),
    syncModule,
    viewModule(config),
)

fun rootModule(): Module = module {
    singleOf(::GlobalKeyboard)
    singleOf(::ApplicationLifecycle)
    singleOf(::MainWindowState)
    // or use includeModules() ;)
}

// not working for compose ViewModels :(
@OptIn(KoinInternalApi::class)
inline fun <reified T : Any> getKoinBeansByType(): List<T> =
    KoinJavaComponent.getKoin().let { koin ->
        koin.instanceRegistry.instances.map { it.value.beanDefinition }
            .filter { it.kind == Kind.Singleton }
            .filter { it.primaryType.isSubclassOf(T::class) }
            .map { koin.get(clazz = it.primaryType, qualifier = null, parameters = null) }
    }
