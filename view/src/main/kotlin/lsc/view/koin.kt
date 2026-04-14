package lsc.view

import lsc.view.internal.DummyViewImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

public fun dummyViewModule() = module {
    singleOf(::DummyViewImpl) bind DummyView::class
}
