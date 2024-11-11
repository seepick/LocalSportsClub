package com.github.christophpickl.localsportsclub.api

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
        val response = http.get(baseUrl)
        response.requireStatusOk()
        val html = HomeLoginParser.parse(response.bodyAsText())
        return HomeResponse(
            loginSecret = html.loginSecret,
            phpSessionId = response.phpSessionId,
        )
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
//            headers {
//                append()
//            }
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
        /*
        <form action="/nl/login" id="login-form" class="smm-login-widget" data-dataLayer-view="{&quot;event&quot;:&quot;login_started&quot;,&quot;user&quot;:{&quot;id&quot;:null,&quot;login_status&quot;:&quot;logged-out&quot;,&quot;membership_city&quot;:null,&quot;membership_country&quot;:null,&quot;membership_status&quot;:null,&quot;membership_plan&quot;:null,&quot;membership_b2b_type&quot;:null,&quot;membership_contract_duration&quot;:null,&quot;company_name&quot;:null}}" data-dataLayer-failure="{&quot;event&quot;:&quot;login_failed&quot;,&quot;user&quot;:{&quot;id&quot;:null,&quot;login_status&quot;:&quot;logged-out&quot;,&quot;membership_city&quot;:null,&quot;membership_country&quot;:null,&quot;membership_status&quot;:null,&quot;membership_plan&quot;:null,&quot;membership_b2b_type&quot;:null,&quot;membership_contract_duration&quot;:null,&quot;company_name&quot;:null},&quot;login_method&quot;:&quot;email&quot;}" method="POST"><input type="hidden" id="dGV0U1RZeEUwZVprV2Z2ZDB3VDZCZz09" name="dGV0U1RZeEUwZVprV2Z2ZDB3VDZCZz09" value="UGlZdWY3RlFFN0RONUdQM1luRmpuUT09" /><input type="hidden" id="check" name="check" /><h5>Inloggen</h5><p><span>Nog geen lid?</span><a href="/nl/prices">Meld je hier aan.</a></p><div id="email-group" class="form-group"><input type="email" id="email" name="email" value="x" class="form-input form-control" placeholder="E-mail *" /></div><div id="password-group" class="form-group"><input type="password" id="password" name="password" value="y" class="form-input form-control" placeholder="Wachtwoord *" /><div class="form-group alert alert-danger">Gebruikersnaam en/of wachtwoord niet correct</div></div><div id="remember-me-group" class="form-group checkbox-group col-xs-6"><label for="remember-me"><input type="checkbox" id="remember-me" name="remember-me" value="1" class="form-control" group-class="checkbox-group col-xs-6" checked="checked" />Onthoud mij</label></div><div id="password-recovery-group" class="form-group col-xs-6"><a href="/nl/password-recovery" id="forgot-password-modal-link" class="forgot-password modal-trigger" data-target="#modal-login" data-toggle="modal">Je wachtwoord vergeten?</a></div><div id="login-group" class="form-group"><input type="submit" id="login" name="login" value="Inloggen" class="usc-button-rebrand usc-button-rebrand--default form_button btn btn-lg btn-primary btn-block" /></div></form>
    <div class="login-failed-datalayer"
         data-datalayer="{&quot;event&quot;:&quot;login_failed&quot;,&quot;user&quot;:{&quot;id&quot;:null,&quot;login_status&quot;:&quot;logged-out&quot;,&quot;membership_city&quot;:null,&quot;membership_country&quot;:null,&quot;membership_status&quot;:null,&quot;membership_plan&quot;:null,&quot;membership_b2b_type&quot;:null,&quot;membership_contract_duration&quot;:null,&quot;company_name&quot;:null},&quot;login_method&quot;:&quot;email&quot;,&quot;error_message&quot;:&quot;Gebruikersnaam en\/of wachtwoord niet correct&quot;}">
    </div>
         */
        // {"success":true,"redirect":"\/nl\/activities"}
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
        val login = body.getElementById("login-form")!!
        val secret = login.getElementsByTag("input").single {
            it.attr("type") == "hidden" && it.id() != "check"
        }
        return HomeHtmlResponse(
            loginSecret = secret.attr("name") to secret.attr("value"),
        )
    }
}
