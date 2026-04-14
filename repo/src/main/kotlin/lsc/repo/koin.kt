package lsc.repo

import lsc.repo.internal.DummyRepoImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

public fun dummyRepoModule() = module {
    singleOf(::DummyRepoImpl) bind DummyRepo::class
}
