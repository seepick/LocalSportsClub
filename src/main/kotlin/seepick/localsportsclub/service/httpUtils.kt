package seepick.localsportsclub.service

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.setCookie
import io.ktor.serialization.kotlinx.json.json
import seepick.localsportsclub.kotlinxSerializer
import java.net.ConnectException

val httpClient = HttpClient(Java) {
    install(ContentNegotiation) {
        json(kotlinxSerializer)
    }
    expectSuccess = false
}

val HttpResponse.phpSessionId: String
    get() = setCookie().singleOrNull { it.name == "PHPSESSID" }?.value ?: error("PHPSESSID cookie is not set!")

suspend fun HttpResponse.requireStatusOk(message: suspend () -> String = { "" }) {
    if (status != HttpStatusCode.OK) {
        throw ApiException("Expected status 200 OK but was [$status] for: ${request.url}. ${message()}")
    }
}

class ApiException(message: String, cause: Exception? = null) : Exception(message, cause)

suspend fun HttpClient.safeGet(url: Url, block: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
    val response = try {
        get(url, block)
    } catch (e: ConnectException) {
        e.printStackTrace()
        error("Failed to GET: $url")
    }
    response.requireStatusOk {
        "Response body was: ${response.bodyAsText()}"
    }
    return response
}
