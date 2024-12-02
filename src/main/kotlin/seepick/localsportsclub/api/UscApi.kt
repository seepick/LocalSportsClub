package seepick.localsportsclub.api

import seepick.localsportsclub.kotlinxSerializer
import seepick.localsportsclub.readFromClasspath
import seepick.localsportsclub.toPrettyString
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


private val httpClient = buildHttpClient()
private fun buildHttpClient(engine: HttpClientEngine = CIO.create()) = HttpClient(engine) {
    install(ContentNegotiation) {
        json(Json {
            isLenient = true
            allowSpecialFloatingPointValues = true
            allowStructuredMapKeys = true
            prettyPrint = true
            useArrayPolymorphism = false
            ignoreUnknownKeys = true
        })
    }
    expectSuccess = false
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

// ---
@Serializable
data class VenuesJsonRoot(
    val success: Boolean,
    val data: VenuesDataJson
)

@Serializable
data class VenuesDataJson(
    val showMore: Boolean, // FIXME request more when this is true!
    // TODO ... add more content ...
)

// ---
enum class District(val label: String, val id: Int, val parent: Int?) {
    Amsterdam("Amsterdam", 8749, null),
    Amsterdam_Centrum("Centrum", 8777, Amsterdam.id),
}

object Playground {
    private val log = logger {}
    private val jsonx = Json {
        prettyPrint = true
    }
    private val apiLang = "nl" // "en"
    private val baseUrl = "https://urbansportsclub.com/$apiLang"

    private val username = System.getProperty("username") ?: error("Please define -Dusername=xxx")
    private val password = System.getProperty("password") ?: error("Please define -password=xxx")

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            // FIXME request "workout filter page": https://urbansportsclub.com/en/online-courses?country=13&date=2024-10-13&plan_type=6
            val result = LoginApi(httpClient, baseUrl).login(username, password)
            when(result) {
                is LoginResult.Failure -> println("FAIL: ${result.message}")
                is LoginResult.Success -> println("SUCCESS: PHP_SESSION_ID=${result.phpSessionId}")
            }
//            requestVenues()
        }
    }


    private suspend fun requestVenues() {
        val response = httpClient.get("$baseUrl/venues") {
            parameter("city_id", "1144")
            parameter("plan_type", "6")
            parameter("show-map", "")
            parameter("business_type[]", "b2c")
            parameter("district[]", District.Amsterdam_Centrum.id)
            header("x-requested-with", "XMLHttpRequest") // IMPORTANT! to change the response to JSON!!!
        }
        log.debug { "${response.status.value} GET ${response.request.url}" }
        logAndPrint(response)
    }

    private fun readVenues() {
        val root = readResponse<VenuesJsonRoot>("venues.json")
        println(root)
    }

    private suspend fun requestStudiosMap() {
        val response = httpClient.get("$baseUrl/studios-map") {
            parameter("city", "1144")
            parameter("plan_type", "6")
        }
        logAndPrint(response)
    }

    private fun readStudiosMap() {
        val root = readResponse<StudiosMapJsonRoot>("studios-map.many.json")
        println(root)
        println(root.data.venues.size)
    }


    private suspend fun logAndPrint(response: HttpResponse) {
        log.debug { "${response.status.value} GET ${response.request.url}" }
        val responseText = response.bodyAsText()
        print(jsonx.toPrettyString(responseText))
    }

    private inline fun <reified T> readResponse(fileName: String): T {
        val json = readFromClasspath("/api2_playground_responses/$fileName")
        return kotlinxSerializer.decodeFromString(json)
    }
}


enum class PlanTypes(val id: Int, val label: String) {
    Small(1, "S"),
    Medium(2, "M"),
    Large(3, "L"),
    ExtraLarge(6, "XL"),
}

class ApiException(message: String, cause: Exception? = null) : Exception(message, cause)
