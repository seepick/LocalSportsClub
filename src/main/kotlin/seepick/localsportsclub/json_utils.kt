package seepick.localsportsclub

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val kotlinxSerializer = Json {
    isLenient = true
    allowSpecialFloatingPointValues = true
    allowStructuredMapKeys = true
    prettyPrint = true
    useArrayPolymorphism = false
    ignoreUnknownKeys = true
}

fun Json.toPrettyString(jsonString: String) =
    encodeToString(parseToJsonElement(jsonString))
