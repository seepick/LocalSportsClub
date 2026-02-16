package seepick.localsportsclub

import kotlinx.serialization.json.Json

@Deprecated("use usc-client")
val serializerLenient = Json {
    isLenient = true
    allowSpecialFloatingPointValues = true
    allowStructuredMapKeys = true
    prettyPrint = true
    useArrayPolymorphism = false
    ignoreUnknownKeys = true
}

@Deprecated("use usc-client")
fun Json.toPrettyString(jsonString: String) =
    encodeToString(parseToJsonElement(jsonString))
