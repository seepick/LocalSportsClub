package seepick.localsportsclub.tools

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Database
import java.io.File

private val log = logger {}

fun connectToDatabase(isProd: Boolean) {
    val home = File(System.getProperty("user.home"))
    val connect = if (isProd) ".lsc" else ".lsc-dev"
    val dbDir = File(home, "$connect/database")
    val jdbcUrl = "jdbc:h2:file:${dbDir.absolutePath}/h2"
    log.info { "Connecting to database: $jdbcUrl" }
    Database.connect(jdbcUrl)
}
