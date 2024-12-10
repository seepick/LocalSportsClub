package seepick.localsportsclub

data class AppConfig(
    val database: DatabaseMode,
) {
    enum class DatabaseMode {
        Exposed, InMemory
    }
    enum class ApiMode {
        NoOp, Simulated, Real
    }
    companion object {
        val development = AppConfig(
            database = DatabaseMode.Exposed
        )
        val production = AppConfig(
            database = DatabaseMode.Exposed
        )
    }
}