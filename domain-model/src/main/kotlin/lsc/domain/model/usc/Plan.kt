package lsc.domain.model.usc

public sealed interface Plan {

    val uscPlan: UscPlan

    public enum class UscPlan(
    ) : Plan {
        Small,
        Medium,
        Large,
        ExtraLarge;

        override val uscPlan = this
        override fun toString() = "Plan.Usc.$name"
    }

    public enum class OnefitPlan(
        override val uscPlan: UscPlan,
    ) : Plan by uscPlan {
        Premium(UscPlan.Large);

        override fun toString() = "Plan.Onefit.$name"
    }
}
