package lsc.domain.model.usc

public data class VisitLimits(
    val small: Int?,
    val medium: Int?,
    val large: Int?,
    val xlarge: Int?,
) {

//    public fun forPlan(plan: Plan.UscPlan): Int? = when (plan) {
//            Plan.UscPlan.Small -> small
//            Plan.UscPlan.Medium -> medium
//            Plan.UscPlan.Large -> large
//            Plan.UscPlan.ExtraLarge -> xlarge
//        }

    companion object // for extensions
}
