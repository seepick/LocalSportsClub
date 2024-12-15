package seepick.localsportsclub

import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType

data class UscConfig(
    val baseUrl: String = "https://urbansportsclub.com/en",
    val city: City = City.Amsterdam,
    val plan: PlanType = PlanType.Large,
)

data class AppConfig(
    val database: DatabaseMode,
    val api: ApiMode,
    val usc: UscConfig = UscConfig()
) {
    enum class DatabaseMode {
        Exposed, InMemory
    }

    enum class ApiMode {
        Mock, Real
    }

    companion object {
        val development = AppConfig(
            database = DatabaseMode.Exposed,
            api = ApiMode.Mock,
        )
        val production = AppConfig(
            database = DatabaseMode.Exposed,
            api = ApiMode.Real,
        )
    }
}
