package seepick.localsportsclub.gcal

import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.GcalMode
import seepick.localsportsclub.LscConfig

fun gcalModule(config: LscConfig) = module {
    single {
        when (config.gcal) {
            GcalMode.Noop -> NoopGcalService
            GcalMode.Real -> PrefsEnabledGcalService(get())
        }
    } bind GcalService::class
}
