package seepick.localsportsclub.service.model

enum class Plan(
    val id: Int, // passed as URL query param
    val apiString: String, // found in JSON
    val label: String,
    val usageInfo: UsageInfo,
) {
    Small(
        1, "S", "Essential", UsageInfo(
            maxCheckins = 4,
            maxOnlineCheckins = 4,
            maxPlusCheckins = 0,
        )
    ),
    Medium(
        2, "M", "Classic", UsageInfo(
            maxCheckins = 10,
            maxOnlineCheckins = 8,
            maxPlusCheckins = 0,
        )
    ),
    Large(
        3,
        "L",
        "Premium",
        UsageInfo(
// Premium OneFit
            maxCheckins = 14,
            maxOnlineCheckins = 8,
            maxPlusCheckins = 4,
            // 2 per day
        )
    ),
    ExtraLarge(
        6, "XL", "Max", UsageInfo(
            maxCheckins = 18,
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
    val maxCheckins: Int, // max monthly, real-life
    val maxOnlineCheckins: Int, // max monthly, internet videostream
    val maxPlusCheckins: Int,
)
