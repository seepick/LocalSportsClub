package seepick.localsportsclub

enum class Environment {
    Production,
    Development;

    companion object {
        val current by lazy {
            prelog("lsc.env = [${System.getProperty("lsc.env")}]")
            val heapSize = Runtime.getRuntime().totalMemory().toDouble()
            prelog("heapSize = ${(heapSize / 1024.0 / 1024.0).toInt()}MB")
            if (System.getProperty("lsc.env") == "PROD") Production else Development
        }
    }
}
