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
            api = ApiMode.RealHttp,
//            api = ApiMode.Mock,

            sync = SyncMode.Real,
//            sync = SyncMode.Noop,
//            sync = SyncMode.Dummy,

            gcal = GcalMode.Real,
//            gcal = GcalMode.Noop,

            versionCheckEnabled = true,
            database = DatabaseMode.Exposed,
            logFileEnabled = true,
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

enum class UscLang(val urlCode: String) {
    English("en"),
    Dutch("nl"),
    German("de"),
    French("fr"),
    // PT, ES
}
