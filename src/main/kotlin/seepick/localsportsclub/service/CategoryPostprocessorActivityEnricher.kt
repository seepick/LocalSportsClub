package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger

class CategoryPostprocessorActivityEnricher : ActivityEnricher {

    private val log = logger {}

    override suspend fun enrich(original: ActivityDbosWithDetails): ActivityDbosWithDetails {
        log.debug { "enrich()" }
        // TODO test-first: override category if title matches
        // make ActivityEnricher infra-code reusable (delegated template pattern)
        return original
    }
}
