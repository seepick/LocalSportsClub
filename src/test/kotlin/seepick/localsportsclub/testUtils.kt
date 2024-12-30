package seepick.localsportsclub

import io.ktor.util.StringValues
import io.ktor.util.toMap
import kotlinx.serialization.json.Json
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.service.date.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun StringValues.toFlatMap(): Map<String, String> =
    toMap().mapValues { it.value.single() }

inline fun <reified T> readTestResponse(fileName: String): T {
    val json = readFromClasspath("/test_lsc/response/$fileName")
    return if (T::class == String::class) json as T else jsonx.decodeFromString(json)
}

val jsonx = Json {
    prettyPrint = true
    ignoreUnknownKeys = false
    isLenient = false
}

class TestableClock(
    private var now: LocalDateTime = LocalDateTime.now(),
    private var today: LocalDate = now.toLocalDate(),
) : Clock {

    fun setSeperately(time: LocalTime, day: LocalDate) {
        now = day.atTime(time)
        today = day
    }

    fun setNowAndToday(now: LocalDateTime) {
        this.now = now
        today = now.toLocalDate()
    }

    override fun now() = now
    override fun today() = today
}

fun ActivityDbo.anotherOne() = copy(id = id + 1, name = "${name}X")
