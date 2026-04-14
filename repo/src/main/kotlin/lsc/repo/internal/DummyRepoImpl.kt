package lsc.repo.internal

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import lsc.repo.DummyRepo

internal class DummyRepoImpl : DummyRepo {

    private val log = logger {}

    override fun foo() {
        println("internal dummy repo sending log...")
        log.info { "Internaly dummy foo()" }
    }
}
