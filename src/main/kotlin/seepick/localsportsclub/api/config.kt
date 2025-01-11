package seepick.localsportsclub.api

import io.ktor.http.Url
import seepick.localsportsclub.UsageConfig
import seepick.localsportsclub.UscLang

data class UscConfig(
    val baseUrl: Url = Url("https://urbansportsclub.com/${UscLang.English.urlCode}"),
    val city: City = City.Amsterdam,
    val plan: PlanType = PlanType.Large,
    val storeResponses: Boolean = true,
    val syncDaysAhead: Int = 14, // including today
    val usageConfig: UsageConfig = UsageConfig(),
) {
    init {
        require(syncDaysAhead >= 1) { "sync days ahead must be >= 1 but was: $syncDaysAhead" }
    }
}
