package allfit.api2

import allfit.service.kotlinxSerializer
import allfit.service.readFromClasspath
import allfit.service.toPrettyString
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
    private val http = buildHttpClient()
    private val jsonx = Json {
        prettyPrint = true
    }
    private val apiLang = "en" // "nl"
    private val baseUrl = "https://urbansportsclub.com/$apiLang"

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            // FIXME request "workout filter page": https://urbansportsclub.com/en/online-courses?country=13&date=2024-10-13&plan_type=6
            requestVenues()
            println()
            println("done")
        }
    }

    private suspend fun requestVenues() {
        val response = http.get("$baseUrl/venues") {
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
        val response = http.get("$baseUrl/studios-map") {
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
