package seepick.localsportsclub.api

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.http.setCookie

val HttpResponse.phpSessionId: String get() = setCookie().singleOrNull { it.name == "PHPSESSID" }?.value ?: error("PHPSESSID cookie is not set!")

fun HttpResponse.requireStatusOk() {
    if(status != HttpStatusCode.OK) {
        throw ApiException("Expected status 200 OK but was $status for URL $request.url")
    }
}

class ApiException(message: String, cause: Exception? = null) : Exception(message, cause)
