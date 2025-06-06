package seepick.localsportsclub

import io.ktor.util.StringValues
import io.ktor.util.toMap
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime

fun StringValues.toFlatMap(): Map<String, String> =
    toMap().mapValues { it.value.single() }

inline fun <reified T> readTestResponse(fileName: String, folder: String = "/test_lsc/response/"): T {
    val fileContent = readFromClasspath("$folder$fileName")
    return if (T::class == String::class) fileContent as T else jsonx.decodeFromString(fileContent)
}

val jsonx = Json {
    prettyPrint = true
    ignoreUnknownKeys = false
    isLenient = false
}

fun LocalDate.atAnyTime() = atTime(LocalTime.of(0, 0))
