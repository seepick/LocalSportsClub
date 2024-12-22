package seepick.localsportsclub

import io.ktor.util.StringValues
import io.ktor.util.toMap
import kotlinx.serialization.json.Json

fun StringValues.toFlatMap(): Map<String, String> =
    toMap().mapValues { it.value.single() }

inline fun <reified T> readTestJson(fileName: String): T {
    val json = readFromClasspath("/test_lsc/$fileName")
    return jsonx.decodeFromString(json)
}

val jsonx = Json {
    prettyPrint = true
    ignoreUnknownKeys = false
    isLenient = false
}

