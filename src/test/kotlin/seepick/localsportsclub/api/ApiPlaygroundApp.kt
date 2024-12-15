package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import seepick.localsportsclub.jsonx
import seepick.localsportsclub.logic.httpClient
import seepick.localsportsclub.readTestJson
import seepick.localsportsclub.toPrettyString

object ApiPlaygroundApp {

    private val log = logger {}

    private val apiLang = "nl" // "en"
    private val baseUrl = "https://urbansportsclub.com/$apiLang"

    private val username by lazy {
        System.getProperty("username") ?: error("Please define -Dusername=xxx")
    }
    private val password by lazy {
        System.getProperty("password") ?: error("Please define -password=xxx")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            // FIXME request "workout filter page": https://urbansportsclub.com/en/online-courses?country=13&date=2024-10-13&plan_type=6
//            val result = LoginApi(httpClient, baseUrl).login(username, password)
//            when (result) {
//                is LoginResult.Failure -> println("FAIL: ${result.message}")
//                is LoginResult.Success -> println("SUCCESS: PHP_SESSION_ID=${result.phpSessionId}")
//            }
//            requestVenues()
//            readStudiosMap()
        }
    }


    private suspend fun requestVenues() {
        val response = httpClient.get("$baseUrl/venues") {
            parameter("city_id", "1144")
            parameter("plan_type", "3")
//            parameter("show-map", "")
//            parameter("business_type[]", "b2c")
            parameter("district[]", District.Amsterdam_Centrum.id)
            header("x-requested-with", "XMLHttpRequest") // IMPORTANT! to change the response to JSON!!!
        }
        log.debug { "${response.status.value} GET ${response.request.url}" }
        logAndPrint(response)
    }

    private suspend fun requestStudiosMap() {
        val response = httpClient.get("$baseUrl/studios-map") {
            parameter("city", "1144")
            parameter("plan_type", "6")
        }
        logAndPrint(response)
    }

    private fun readStudiosMap() {
        val root = readTestJson<StudiosMapJsonRoot>("response_studios_map.many.json")
        println(root)
        println(root.data.venues.size)
    }


    private suspend fun logAndPrint(response: HttpResponse) {
        log.debug { "${response.status.value} GET ${response.request.url}" }
        val responseText = response.bodyAsText()
        print(jsonx.toPrettyString(responseText))
    }

}


@Serializable
data class StudiosMapJsonRoot(
    val success: Boolean,
    val data: StudiosMapJsonObject,
)

@Serializable
data class StudiosMapJsonObject(
    val venues: List<StudiosMapVenue>
)

@Serializable
data class StudiosMapVenue(
    val id: Int,
    val name: String,
    val address: String,
    // addressId: Int,
    /** Used to calculate the details URL. */
    val slug: String,
    // location: lat/lng
    // district: West
    // planTypeIds
    // studioCovers
    // categories
    // - id, key, name, is_top_category, icon, category_group_id, translations, plan types...
    // featured
)
