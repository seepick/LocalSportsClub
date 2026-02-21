package seepick.localsportsclub.gcal

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun gcalModule() = module {
    singleOf(::PrefsEnabledGcalService) bind GcalService::class
}
