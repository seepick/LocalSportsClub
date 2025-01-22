package seepick.localsportsclub.tools

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.Database
import java.io.File

private val log = logger {}

class ProdAbortedException() : Exception("User aborted the process to execute DB prod connection.")

fun cliConnectToDatabase(isProd: Boolean) {
    if (isProd) {
        println("You are about to apply changes to PRODUCTION. Are you sure?")
        print("[y|N]>> ")
        val read = readln()
        if (read != "y") {
            println("Aborted...")
            throw ProdAbortedException()
        }
    }

    val home = File(System.getProperty("user.home"))
    val connect = if (isProd) ".lsc" else ".lsc-dev"
    val dbDir = File(home, "$connect/database")
    val jdbcUrl = "jdbc:h2:file:${dbDir.absolutePath}/h2"
    log.info { "Connecting to database: $jdbcUrl" }
    Database.connect(jdbcUrl)
}
