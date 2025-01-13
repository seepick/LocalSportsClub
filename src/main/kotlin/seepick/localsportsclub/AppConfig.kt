package seepick.localsportsclub

import seepick.localsportsclub.api.UscConfig
import seepick.localsportsclub.gcal.GcalConfig
import seepick.localsportsclub.view.Screen

data class AppConfig(
    val database: DatabaseMode,
    val api: ApiMode,
    val sync: SyncMode,
    val firstScreen: Screen? = null,
    val usc: UscConfig = UscConfig(),
    val logFileEnabled: Boolean = false,
    val gcal: GcalMode,
    val gcalConfig: GcalConfig = GcalConfig(),
) {
    companion object {
        val development = AppConfig(
//            api = ApiMode.RealHttp,
            api = ApiMode.Mock,

//            sync = SyncMode.Real,
//            sync = SyncMode.Noop,
            sync = SyncMode.Dummy,

            gcal = GcalMode.Real,
//            gcal = GcalMode.Noop,

            database = DatabaseMode.Exposed,
            logFileEnabled = true,
//            firstScreen = Screen.Activities,
        )
        val production = AppConfig(
            database = DatabaseMode.Exposed,
            api = ApiMode.RealHttp,
            gcal = GcalMode.Real,
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

enum class GcalMode {
    Noop, Real
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
