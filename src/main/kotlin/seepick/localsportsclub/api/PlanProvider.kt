package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.plan.MembershipApi
import seepick.localsportsclub.service.model.Plan
import seepick.localsportsclub.service.singles.SinglesService

class PlanProvider(
    private val singlesService: SinglesService,
    private val membershipApi: MembershipApi,
) {
    private val log = logger {}

    private var cached: Plan? = null

    suspend fun provide(sessionId: PhpSessionId): Plan {
        log.debug { "provide(..)" }
        if (cached == null) {
            val storedPlan = singlesService.plan
            cached = if (storedPlan == null) {
                val fetched = membershipApi.fetch(sessionId).plan
                singlesService.plan = fetched
                fetched
            } else {
                storedPlan
            }
        }
        return cached!!
    }
}
