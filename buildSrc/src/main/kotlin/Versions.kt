@Suppress("MayBeConstant", "unused", "ClassName", "ConstPropertyName")
object Versions {
    const val java = 17
    val kotlin = "2.3.10"

    val exposed = "1.0.0"
    val h2 = "2.3.232"
    val hoplite = "3.0.0.RC1" // "2.9.0"
    val mockk = "1.14.6"
    val koin = "4.0.2" // NO! 4.1.1 UnsatisfiedLinkError
    val ktor = "3.3.2"
    // val uscClient = "2026.3.3"
    val uscClient = "2000.0.SNAPSHOT" // FIXME change to next release version once there
    val testcontainers = "2.0.2"

    object logging {
        val kotlin = "8.0.01"
        val logback = "1.5.32"
    }

    object testing {
        val junit = "6.0.1"
        val kotest = "6.1.3"
    }
}
