package seepick.localsportsclub.logic

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val logicModule = module {
    singleOf(::MemoryReadWrite) {
        bind<Memory>()
        createdAtStart()
    }
    singleOf(::ServiceImpl) bind Service::class
}
