package seepick.localsportsclub

data class AppConfig(
    val mockDb: Boolean,
) {
    companion object {
        val development = AppConfig(
            mockDb = false
        )
        val production = AppConfig(
            mockDb = false
        )
    }
}