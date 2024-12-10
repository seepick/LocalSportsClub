package seepick.localsportsclub.api

import io.kotest.common.runBlocking
import seepick.localsportsclub.logic.httpClient

object LoginManualSystemTest {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            manualTest()
        }
    }

    private suspend fun manualTest() {
        val username = System.getProperty("username") ?: error("Define: -Dusername=xxx")
        val password = System.getProperty("password") ?: error("Define: -Dpassword=xxx")
        val api = LoginApi(httpClient, "https://urbansportsclub.com/en")
        val result = api.login(username, password)
        println(result)
    }
}
