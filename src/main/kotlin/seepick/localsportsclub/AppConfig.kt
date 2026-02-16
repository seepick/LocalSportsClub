package seepick.localsportsclub

import seepick.localsportsclub.api.UscConfig

data class AppConfig(
    val database: DatabaseMode,
    val api: ApiMode,
    val sync: SyncMode,
    val usc: UscConfig = UscConfig(),
    val logFileEnabled: Boolean = false,
    val gcal: GcalMode,
    val versionCheckEnabled: Boolean = true,
) {
    companion object {
        val development = AppConfig(
//            api = ApiMode.Mock,
            api = ApiMode.RealHttp,

//            sync = SyncMode.Dummy,
            sync = SyncMode.Real,
//            sync = SyncMode.Noop,
//            sync = SyncMode.Delayed,

//            gcal = GcalMode.Real,
            gcal = GcalMode.Noop,

            versionCheckEnabled = false,
            database = DatabaseMode.Exposed,
//            database = DatabaseMode.InMemory,
            logFileEnabled = false,
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

@Deprecated("use usc-client")
enum class ApiMode {
    Mock, RealHttp
}

enum class SyncMode {
    Noop, Delayed, Dummy, Real
}

enum class GcalMode {
    Noop, Real
}

@Deprecated("use usc-client")
enum class UscLang(val urlCode: String) {
    English("en"),
    Dutch("nl"),
//    German("de"),
//    French("fr"),
    // PT, ES
}
