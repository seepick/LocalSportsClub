package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.request.cookie
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.parameters
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.jsoup.Jsoup

class LoginApi(
    private val http: HttpClient,
    private val baseUrl: String,
) {

    private val log = logger {}

    suspend fun login(username: String, password: String): LoginResult {
        log.info { "login($username)" }
        val home = loadHome()
        return submitLogin(
            LoginRequest(
                email = username,
                password = password,
                phpSessionId = home.phpSessionId,
                secret = home.loginSecret,
            )
        )
    }

    private data class HomeResponse(
        val loginSecret: Pair<String, String>,
        val phpSessionId: String,
    )

    private suspend fun loadHome(): HomeResponse {
        log.debug { "Requesting home to extract basic session info." }
        val response = http.get(baseUrl)
        response.requireStatusOk()
        val html = HomeLoginParser.parse(response.bodyAsText())
        return HomeResponse(
            loginSecret = html.loginSecret,
            phpSessionId = response.phpSessionId,
        ).also {
            log.debug { "Extracted: $it" }
        }
    }

    private data class LoginRequest(
        val email: String,
        val password: String,
        val phpSessionId: String,
        val secret: Pair<String, String>,
    )

    private suspend fun submitLogin(login: LoginRequest): LoginResult {
        val response = http.submitForm(
            url = "$baseUrl/login",
            formParameters = parameters {
                append("email", login.email)
                append("password", login.password)
                append(login.secret.first, login.secret.second)
                append("check", "")
                append("remember-me", "1")
            }
        ) {
            cookie("PHPSESSID", login.phpSessionId)
            cookie("usc_city_selected", "1")
            cookie(
                "_tracking_consent",
                "%7B%22con%22%3A%7B%22CMP%22%3A%7B%22a%22%3A%22%22%2C%22m%22%3A%22%22%2C%22p%22%3A%22%22%2C%22s%22%3A%22%22%7D%7D%2C%22v%22%3A%222.1%22%2C%22region%22%3A%22NLNH%22%2C%22reg%22%3A%22GDPR%22%7D"
            )
            header("x-requested-with", "XMLHttpRequest") // IMPORTANT! to change the response to JSON!!!
            header("accept-language", "nl-NL,nl;q=0.8")
            header("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
            header("origin", "https://urbansportsclub.com")
            header("referer", "https://urbansportsclub.com/nl/")
            header("priority", "u=1, i")
            header("accept", "*/*")
            header("accept-language", "nl-NL,nl;q=0.5")
            header("sec-ch-ua", "\"Chromium\";v=\"130\", \"Brave\";v=\"130\", \"Not?A_Brand\";v=\"99\"")
            header("sec-ch-ua-mobile", "?0")
            header("sec-ch-ua-platform", "macOS")
            header("sec-fetch-dest", "empty")
            header("sec-fetch-mode", "cors")
            header("sec-fetch-site", "same-origin")
            header("sec-gpc", "1")
            header(
                "user-agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"
            )
        }
        response.requireStatusOk()
        val jsonSuccessOrHtmlFail = response.bodyAsText()
        try {
            val jsonRoot = Json.parseToJsonElement(jsonSuccessOrHtmlFail)
            LoginResult.Success(phpSessionId = login.phpSessionId)
            return if(jsonRoot.jsonObject["success"].toString() == "true") {
                LoginResult.Success(phpSessionId = login.phpSessionId)
            } else {
                log.warn { "Returned JSON after login:\n$jsonSuccessOrHtmlFail" }
                LoginResult.Failure("Invalid JSON returned!")
            }
        } catch (e: SerializationException) {
            return LoginResult.Failure("Seems username/password is wrong.")
        }
    }
}

sealed interface LoginResult {
    data class Success(val phpSessionId: String) : LoginResult
    data class Failure(val message: String) : LoginResult
}

object HomeLoginParser {

    data class HomeHtmlResponse(
        val loginSecret: Pair<String, String>, // hidden input in the login form, which needs to be passed through
    )

    fun parse(html: String): HomeHtmlResponse {
        val body = Jsoup.parse(html).body()
        val login = body.getElementById("login-form") ?: error("login-form not found in HTML response:\n\n$html")
        val secret = login.getElementsByTag("input").single {
            it.attr("type") == "hidden" && it.id() != "check"
        }
        return HomeHtmlResponse(
            loginSecret = secret.attr("name") to secret.attr("value"),
        )
    }
}
