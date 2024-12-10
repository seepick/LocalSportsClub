package seepick.localsportsclub

import io.ktor.util.StringValues
import io.ktor.util.toMap

fun StringValues.toFlatMap(): Map<String, String> =
    toMap().mapValues { it.value.single() }
