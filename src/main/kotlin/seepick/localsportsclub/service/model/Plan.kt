package seepick.localsportsclub.service.model

enum class Plan(
    val id: Int, // passed as URL query param
    val apiString: String, // found in JSON
    val label: String,
    val usageInfo: UsageInfo,
) {
    Small(
        id = 1,
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
        apiString = "XL",
        label = "Max",
        usageInfo = UsageInfo(
            maxCheckinsInPeriod = 18,
            maxOnlineCheckins = 8,
            maxPlusCheckins = 8,
        )
    );

    companion object {
        fun byApiString(search: String): Plan =
            Plan.entries.single { it.apiString == search }
    }
}

data class UsageInfo(
    val maxCheckinsInPeriod: Int, // max monthly, real-life
    val maxOnlineCheckins: Int, // max monthly, internet videostream
    val maxPlusCheckins: Int,
)
