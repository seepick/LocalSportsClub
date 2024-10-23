package com.github.christophpickl.localsportsclub

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val kotlinxSerializer = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

fun Json.toPrettyString(jsonString: String) =
    encodeToString(parseToJsonElement(jsonString))

