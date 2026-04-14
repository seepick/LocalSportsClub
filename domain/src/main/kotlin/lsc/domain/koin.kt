package lsc.domain

import lsc.domain.internal.DummyServiceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

public fun dummyServiceModule() = module {
    singleOf(::DummyServiceImpl) bind DummyService::class
}
