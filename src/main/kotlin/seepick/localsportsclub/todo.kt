package seepick.localsportsclub

//public interface PlanRepository {
//    fun selectPlan(): Plan?
//    fun updatePlan(plan: Plan)
//}
//internal class CachedPlanOrFetchProvider(
//    private val planRepo: PlanRepository,
//    private val membershipApi: MembershipApi,
//) {
//    private val log = logger {}
//
//    private var cached: Plan? = null
//
//    // TODO not used internally?! thus in LSC only; refactor (or maybe use in here, to simplify sync infra...?)
//    suspend fun provide(sessionId: PhpSessionId): Plan {
//        log.debug { "provide(..)" }
//        if (cached == null) {
//            val storedPlan = planRepo.selectPlan()
//            cached = if (storedPlan == null) {
//                val fetched = membershipApi.fetch(sessionId).plan
//                planRepo.updatePlan(fetched)
//                fetched
//            } else {
//                storedPlan
//            }
//        }
//        return cached!!
//    }
//}
