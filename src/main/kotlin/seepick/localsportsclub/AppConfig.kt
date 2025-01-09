package seepick.localsportsclub

import io.ktor.http.Url
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.view.Screen

data class AppConfig(
    val database: DatabaseMode,
    val api: ApiMode,
    val sync: SyncMode,
    val firstScreen: Screen? = null,
    val usc: UscConfig = UscConfig(),
    val logFileEnabled: Boolean = false,
) {
    companion object {
        val development = AppConfig(
            api = ApiMode.RealHttp,
            sync = SyncMode.Real,
//            api = ApiMode.Mock,
//            sync = SyncMode.Noop,

            database = DatabaseMode.Exposed,
            logFileEnabled = true,
//            firstScreen = Screen.Activities,
        )
        val production = AppConfig(
            database = DatabaseMode.Exposed,
            api = ApiMode.RealHttp,
            sync = SyncMode.Real,
            logFileEnabled = true,
        )

        val downloadImageSize = 400 to 400
    }
}

enum class DatabaseMode {
    Exposed, InMemory
}

enum class ApiMode {
    Mock, RealHttp
}

enum class SyncMode {
    Noop, Delayed, Dummy, Real
}

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

data class UsageConfig(
    val periodConfiguredFirstDay: Int = 2,

    val maxBookingsForPeriod: Int = 18,
    val maxBookingsForDay: Int = 2,
    val maxBookingsPerVenueForMonth: Int = 6, // it's per venue, altough a parther can have multiple venues (=locations)
    val maxBookingsPerVenueForDay: Int = 1,
)

enum class UscLang(val urlCode: String) {
    English("en"),
    Dutch("nl"),
    German("de"),
    French("fr"),
    // PT, ES
}
