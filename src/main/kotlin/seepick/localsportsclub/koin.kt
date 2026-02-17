package seepick.localsportsclub

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.UscApiDeferred
import com.github.seepick.uscclient.UscApiMock
import com.github.seepick.uscclient.UscConfig
import com.github.seepick.uscclient.UscConnector
import com.github.seepick.uscclient.UscConnectorMock
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.gcal.gcalModule
import seepick.localsportsclub.persistence.persistenceModule
import seepick.localsportsclub.service.serviceModule
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.sync.syncModule
import seepick.localsportsclub.view.viewModule

private val log = logger {}

fun allModules(config: LscConfig) = listOf(
    rootModule(config),
    persistenceModule(config.database),
    uscClientModule(config),
    serviceModule(config),
    gcalModule(config),
    syncModule(config),
    viewModule(config),
)

fun rootModule(config: LscConfig): Module = module {
    singleOf(::GlobalKeyboard)
    singleOf(::ApplicationLifecycle)
    singleOf(::MainWindowState)
    // or use includeModules() ;)
}

fun uscClientModule(config: LscConfig) = module {
    if (config.apiMode == ApiMode.Mock) {
        log.debug { "Wiring mocked USC API." }
        singleOf(::UscApiMock) bind UscApi::class
        single { UscConnectorMock() } bind UscConnector::class

    } else if (config.apiMode == ApiMode.RealHttp) {
        single { UscConnector() } bind UscConnector::class
        single {
            UscApiDeferred({
                val singlesService: SinglesService by inject()
                UscConfig(
                    credentials = singlesService.verifiedUscCredentials ?: error("No verified credentials stored!"),
                    responseLogFolder = config.responseLogFolder,
                    currentYear = config.currentYear,
                )
            })
        } bind UscApi::class
    }
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
