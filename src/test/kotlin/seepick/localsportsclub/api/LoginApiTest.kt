package seepick.localsportsclub.api

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import seepick.localsportsclub.readFromClasspath

class LoginApiTest : StringSpec() {
    private val baseUrl = "http://baseurl.test"
    private val username = "username"
    private val password = "password"
    private val testSessionId = "phpSessIdTestValue"
    private val loginResponseSuccess = """{"success":true,"redirect":"\/nl\/activities"}"""
    private val loginResponseFail = """
        <form action="/nl/login" id="login-form" class="smm-login-widget" data-dataLayer-view="{&quot;event&quot;:&quot;login_started&quot;,&quot;user&quot;:{&quot;id&quot;:null,&quot;login_status&quot;:&quot;logged-out&quot;,&quot;membership_city&quot;:null,&quot;membership_country&quot;:null,&quot;membership_status&quot;:null,&quot;membership_plan&quot;:null,&quot;membership_b2b_type&quot;:null,&quot;membership_contract_duration&quot;:null,&quot;company_name&quot;:null}}" data-dataLayer-failure="{&quot;event&quot;:&quot;login_failed&quot;,&quot;user&quot;:{&quot;id&quot;:null,&quot;login_status&quot;:&quot;logged-out&quot;,&quot;membership_city&quot;:null,&quot;membership_country&quot;:null,&quot;membership_status&quot;:null,&quot;membership_plan&quot;:null,&quot;membership_b2b_type&quot;:null,&quot;membership_contract_duration&quot;:null,&quot;company_name&quot;:null},&quot;login_method&quot;:&quot;email&quot;}" method="POST"><input type="hidden" id="dGV0U1RZeEUwZVprV2Z2ZDB3VDZCZz09" name="dGV0U1RZeEUwZVprV2Z2ZDB3VDZCZz09" value="UGlZdWY3RlFFN0RONUdQM1luRmpuUT09" /><input type="hidden" id="check" name="check" /><h5>Inloggen</h5><p><span>Nog geen lid?</span><a href="/nl/prices">Meld je hier aan.</a></p><div id="email-group" class="form-group"><input type="email" id="email" name="email" value="x" class="form-input form-control" placeholder="E-mail *" /></div><div id="password-group" class="form-group"><input type="password" id="password" name="password" value="y" class="form-input form-control" placeholder="Wachtwoord *" /><div class="form-group alert alert-danger">Gebruikersnaam en/of wachtwoord niet correct</div></div><div id="remember-me-group" class="form-group checkbox-group col-xs-6"><label for="remember-me"><input type="checkbox" id="remember-me" name="remember-me" value="1" class="form-control" group-class="checkbox-group col-xs-6" checked="checked" />Onthoud mij</label></div><div id="password-recovery-group" class="form-group col-xs-6"><a href="/nl/password-recovery" id="forgot-password-modal-link" class="forgot-password modal-trigger" data-target="#modal-login" data-toggle="modal">Je wachtwoord vergeten?</a></div><div id="login-group" class="form-group"><input type="submit" id="login" name="login" value="Inloggen" class="usc-button-rebrand usc-button-rebrand--default form_button btn btn-lg btn-primary btn-block" /></div></form>
    <div class="login-failed-datalayer"
         data-datalayer="{&quot;event&quot;:&quot;login_failed&quot;,&quot;user&quot;:{&quot;id&quot;:null,&quot;login_status&quot;:&quot;logged-out&quot;,&quot;membership_city&quot;:null,&quot;membership_country&quot;:null,&quot;membership_status&quot;:null,&quot;membership_plan&quot;:null,&quot;membership_b2b_type&quot;:null,&quot;membership_contract_duration&quot;:null,&quot;company_name&quot;:null},&quot;login_method&quot;:&quot;email&quot;,&quot;error_message&quot;:&quot;Gebruikersnaam en\/of wachtwoord niet correct&quot;}">
    </div>"""

    init {
        "Given successful login response When login Then succeed and return session Id" {
            val result = LoginApi(mockedClient(isSuccess = true), baseUrl).login(username, password)

            result.shouldBeInstanceOf<LoginResult.Success>().phpSessionId shouldBe testSessionId
        }
        "Given failing login response When login Then fail" {
            val result = LoginApi(mockedClient(isSuccess = false), baseUrl).login(username, password)

            result.shouldBeInstanceOf<LoginResult.Failure>()
        }
    }

    private fun mockedClient(isSuccess: Boolean) =
        HttpClient(MockEngine { request ->
            when (val requestUrl = request.url.toString()) {
                baseUrl -> respond(
                    content = readFromClasspath("/test_lsc/response_home.html"),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.SetCookie, "PHPSESSID=$testSessionId")
                )

                "$baseUrl/login" -> respond(
                    content = if (isSuccess) loginResponseSuccess else loginResponseFail,
                    status = HttpStatusCode.OK,
                )

                else -> error("Unhandled request URL: [$requestUrl]")
            }
        })
}
