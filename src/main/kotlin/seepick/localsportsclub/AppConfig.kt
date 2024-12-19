package seepick.localsportsclub

import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType

data class UscConfig(
    val baseUrl: String = "https://urbansportsclub.com/${UscLang.English.urlCode}",
    val city: City = City.Amsterdam,
    val plan: PlanType = PlanType.Large,
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
    val usc: UscConfig = UscConfig()
) {
    enum class DatabaseMode {
        Exposed, InMemory
    }

    enum class ApiMode {
        Mock, Real
    }

    enum class SyncMode {
        Noop, Simulated, Real
    }

    companion object {
        val development = AppConfig(
            database = DatabaseMode.Exposed,
            api = ApiMode.Mock,
            sync = SyncMode.Noop,
        )
        val production = AppConfig(
            database = DatabaseMode.Exposed,
            api = ApiMode.Real,
            sync = SyncMode.Real,
        )
    }
}
