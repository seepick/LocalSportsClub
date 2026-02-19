package seepick.localsportsclub.gcal

import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.GcalMode
import seepick.localsportsclub.LscConfig

fun gcalModule(config: LscConfig) = module {
    single {
        when (config.gcalMode) {
            GcalMode.Noop -> NoopGcalService
            GcalMode.Real -> PrefsEnabledGcalService(get(), get())
        }
    } bind GcalService::class
}
