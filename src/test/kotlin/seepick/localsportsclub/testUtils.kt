package seepick.localsportsclub

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.ktor.http.Url
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

private val imageExtensions = listOf("jpg", "png")
fun Arb.Companion.imageFileExtension() = arbitrary {
    imageExtensions.random()
}

fun Arb.Companion.url() = arbitrary {
    Url(
        "https://${
            string(
                minSize = 5,
                maxSize = 15,
                codepoints = Codepoint.alphanumeric()
            ).next()
        }.${string(minSize = 3, maxSize = 3, codepoints = Codepoint.az()).next()}"
    )
}

fun Arb.Companion.imageUrl() = arbitrary {
    val fileName =
        string(minSize = 3, maxSize = 15, codepoints = Codepoint.az()).next() + "." + imageFileExtension().next()
    Url("${url().next()}/${fileName}")
}

