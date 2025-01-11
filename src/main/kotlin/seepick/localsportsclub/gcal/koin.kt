package seepick.localsportsclub.gcal

import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.GcalMode

fun gcalModule(config: AppConfig) = module {
    single {
        when (config.gcal) {
            GcalMode.Noop -> NoopGcalService
            GcalMode.Real -> RealGcalService(
                calendarId = config.gcalConfig.calendarId,
            )
        }
    } bind GcalService::class
}
