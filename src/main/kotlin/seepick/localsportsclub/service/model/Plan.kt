package seepick.localsportsclub.service.model

sealed interface Plan {

    companion object {
        fun byInternalId(internalId: String): Plan =
            UscPlan.entries.singleOrNull { it.internalId == internalId }
                ?: OnefitPlan.entries.singleOrNull { it.internalId == internalId }
                ?: error("Invalid internal plan ID: [${internalId}]!")
    }

    val id: Int // passed as URL query param
    val internalId: String
    val apiString: String // found in JSON
    val label: String
    val usageInfo: UsageInfo

    enum class UscPlan(
        override val id: Int,
        override val internalId: String,
        override val apiString: String,
        override val label: String,
        override val usageInfo: UsageInfo,
    ) : Plan {
        Small(
            id = 1,
            internalId = "uscSmall",
            apiString = "S",
            label = "Essential",
            usageInfo = UsageInfo(
                maxCheckinsInPeriod = 4,
                maxOnlineCheckins = 4,
                maxPlusCheckins = 0,
            )
        ),
        Medium(
            id = 2,
            internalId = "uscMedium",
            apiString = "M",
            label = "Classic",
            usageInfo = UsageInfo(
                maxCheckinsInPeriod = 10,
                maxOnlineCheckins = 8,
                maxPlusCheckins = 0,
            )
        ),
        Large(
            id = 3,
            internalId = "uscLarge",
            apiString = "L",
            label = "Premium",
            usageInfo = UsageInfo(
                maxCheckinsInPeriod = 14,
                maxOnlineCheckins = 8,
                maxPlusCheckins = 4,
                // 2 per day
            )
        ),
        ExtraLarge(
            id = 6,
            internalId = "uscExtraLarge",
            apiString = "XL",
            label = "Max",
            usageInfo = UsageInfo(
                maxCheckinsInPeriod = 18,
                maxOnlineCheckins = 8,
                maxPlusCheckins = 8,
            )
        );

        companion object {
            fun byApiString(id: String) = entries.single { it.apiString == id }
        }

        override fun toString() = "Plan.Usc.$name"
    }

    enum class OnefitPlan(
        private val uscPlan: UscPlan
    ) : Plan by uscPlan {
        Premium(UscPlan.Large) {
            override val internalId = "onefitPremium"
            override val label = "Onefit Premium"
            override val usageInfo = UsageInfo(
                maxCheckinsInPeriod = 18,
                maxOnlineCheckins = 8,
                maxPlusCheckins = 4,
            )
        };

        override fun toString() = "Plan.Onefit.$name"
    }
}

data class UsageInfo(
    val maxCheckinsInPeriod: Int, // max monthly, real-life
    val maxOnlineCheckins: Int, // max monthly, internet videostream
    val maxPlusCheckins: Int,
)
