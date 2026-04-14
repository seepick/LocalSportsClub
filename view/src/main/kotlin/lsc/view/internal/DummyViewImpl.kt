package lsc.view.internal

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import lsc.domain.DummyService
import lsc.view.DummyView

internal class DummyViewImpl(
    private val dummyService: DummyService,
) : DummyView {

    private val log = logger {}

    override fun baz() {
        println("internal dummy view sending log...")
        log.info { "Internaly dummy baz()->bar()" }
        dummyService.bar()
    }
}
