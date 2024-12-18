package seepick.localsportsclub.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import seepick.localsportsclub.kotlinxSerializer

val httpClient = buildHttpClient()

private fun buildHttpClient(engine: HttpClientEngine = CIO.create()) = HttpClient(engine) {
    install(ContentNegotiation) {
        json(kotlinxSerializer)
    }
    expectSuccess = false
}
