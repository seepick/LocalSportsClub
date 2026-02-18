package seepick.localsportsclub.persistence

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
