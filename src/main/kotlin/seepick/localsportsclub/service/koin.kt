package seepick.localsportsclub.service

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.api.domain.VenuesService
import seepick.localsportsclub.api.domain.VenuesServiceImpl

fun logicModule() = module {
    singleOf(::VenuesServiceImpl) bind VenuesService::class
}
