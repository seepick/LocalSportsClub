package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.serialization.encodeToString
import seepick.localsportsclub.kotlinxSerializer
import seepick.localsportsclub.service.FileEntry
import seepick.localsportsclub.service.FileResolver


object CredentialsManagerApp {
    @JvmStatic
    fun main(args: Array<String>) {
        print("Username: ")
        val username = readln().trim()
        print("Password: ")
        val password = readln().trim()

        save(Credentials(username = username, password = password))
    }

    private val log = logger {}

    private fun save(credentials: Credentials) {
        val file = FileResolver.resolve(FileEntry.Login)
        log.debug { "Storing credentials to file: ${file.absolutePath}" }
        file.writeText(
            kotlinxSerializer.encodeToString(
                LoginJson(
                    username = credentials.username,
                    password = Encrypter.encrypt(credentials.password),
                )
            )
        )
    }


}
