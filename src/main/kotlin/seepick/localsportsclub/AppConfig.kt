package seepick.localsportsclub

import io.ktor.http.Url
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.view.Screen

data class UscConfig(
    val baseUrl: Url = Url("https://urbansportsclub.com/${UscLang.English.urlCode}"),
    val city: City = City.Amsterdam,
    val plan: PlanType = PlanType.Large,
    val storeResponses: Boolean = true,
    val syncDaysAhead: Int = 3,
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
    val maxBookingsPerVenueForMonth: Int = 6, // TODO or is it per partner (a partner having multiple, linked venues?!)
    val maxBookingsPerVenueForDay: Int = 1,
)

enum class UscLang(val urlCode: String) {
    English("en"),
    Dutch("nl"),
    German("de"),
    French("fr"),
    // PT, ES
}

data class AppConfig(
    val database: DatabaseMode,
    val api: ApiMode,
    val sync: SyncMode,
    val firstScreen: Screen? = null,
    val usc: UscConfig = UscConfig(),
    val logFileEnabled: Boolean = false,
) {
    enum class DatabaseMode {
        Exposed, InMemory
    }

    enum class ApiMode {
        Mock, RealHttp
    }

    enum class SyncMode {
        Noop, Delayed, Dummy, Real
    }

    companion object {
        val development = AppConfig(
            database = DatabaseMode.Exposed,
//            api = ApiMode.RealHttp,
//            sync = SyncMode.Real,
            api = ApiMode.Mock,
            sync = SyncMode.Dummy,

            logFileEnabled = true,
            firstScreen = Screen.Activities,
        )
        val production = AppConfig(
            database = DatabaseMode.Exposed,
            api = ApiMode.RealHttp,
            sync = SyncMode.Real,
            logFileEnabled = true,
        )
    }
}
