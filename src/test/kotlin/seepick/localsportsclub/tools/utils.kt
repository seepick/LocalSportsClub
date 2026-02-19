package seepick.localsportsclub.tools

import seepick.localsportsclub.LscConfig
import seepick.localsportsclub.development
import seepick.localsportsclub.persistence.connect
import seepick.localsportsclub.production
import seepick.localsportsclub.service.DirectoryEntry

class ProdAbortedException : Exception("User aborted the process to execute DB prod connection.")

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

    connectToDatabaseEnvAware(isProd)
}

fun connectToDatabaseEnvAware(isProd: Boolean) {
    val config = if (isProd) LscConfig.production() else LscConfig.development
    connect(config.fileResolver.resolve(DirectoryEntry.Database), liquibaseEnabled = false)
}
