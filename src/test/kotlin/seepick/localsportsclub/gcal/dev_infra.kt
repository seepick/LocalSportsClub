package seepick.localsportsclub.gcal

import org.koin.dsl.bind
import org.koin.dsl.module

fun noopGcalModule() = module {
    single { NoopGcalService } bind GcalService::class
}
