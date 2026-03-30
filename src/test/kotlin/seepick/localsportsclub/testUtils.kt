package seepick.localsportsclub

import com.github.seepick.uscclient.shared.DateTimeRange
import io.ktor.util.StringValues
import io.ktor.util.toMap
import kotlinx.serialization.json.Json
import seepick.localsportsclub.service.readFromClasspath
import java.time.LocalDate
import java.time.LocalTime

fun StringValues.toFlatMap(): Map<String, String> =
    toMap().mapValues { it.value.single() }

inline fun <reified T> readTestResponse(fileName: String, folder: String = "/responses/"): T {
    val fileContent = readFromClasspath("$folder${fileName.trimStart('/')}")
    return if (T::class == String::class) fileContent as T else jsonx.decodeFromString(fileContent)
}

val jsonx = Json {
    prettyPrint = true
    ignoreUnknownKeys = false
    isLenient = false
}

fun LocalDate.atAnyTime() = atTime(LocalTime.of(0, 0))

fun DateTimeRange.adjustHours(hoursToAdd: Int) = copy(
    from = from.plusHours(hoursToAdd.toLong()),
    to = to.plusHours(hoursToAdd.toLong()),
)
