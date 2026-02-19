package seepick.localsportsclub.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val jsonSerializer = Json {
    isLenient = true
    allowSpecialFloatingPointValues = true
    allowStructuredMapKeys = true
    prettyPrint = true
    useArrayPolymorphism = false
    ignoreUnknownKeys = true
}

fun buildHttpClient(): HttpClient = HttpClient(Apache5) {
    install(ContentNegotiation) {

        json(jsonSerializer)
    }
    expectSuccess = false
}
