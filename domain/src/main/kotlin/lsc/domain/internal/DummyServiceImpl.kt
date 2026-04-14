package lsc.domain.internal

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import lsc.domain.DummyService
import lsc.repo.DummyRepo

internal class DummyServiceImpl(
    private val dummyRepo: DummyRepo,
) : DummyService {

    private val log = logger {}

    override fun bar() {
        println("internal dummy service sending log...")
        log.info { "Internaly dummy bar()->foo()" }
        dummyRepo.foo()
    }
}
