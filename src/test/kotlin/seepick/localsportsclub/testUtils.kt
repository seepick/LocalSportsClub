package seepick.localsportsclub

import io.ktor.util.StringValues
import io.ktor.util.toMap
import kotlinx.serialization.json.Json

fun StringValues.toFlatMap(): Map<String, String> =
    toMap().mapValues { it.value.single() }

inline fun <reified T> readTestResponse(fileName: String, folder: String = "/test_lsc/response/"): T {
    val json = readFromClasspath("$folder$fileName")
    return if (T::class == String::class) json as T else jsonx.decodeFromString(json)
}

val jsonx = Json {
    prettyPrint = true
    ignoreUnknownKeys = false
    isLenient = false
}
