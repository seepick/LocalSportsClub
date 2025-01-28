package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.min
import kotlin.reflect.full.isSuperclassOf

private val log = logger {}

suspend fun <T, R> workParallel(
    coroutineCount: Int,
    data: List<T>,
    processor: suspend (T) -> R,
): List<R> = coroutineScope {
    withContext(Dispatchers.IO) {
        val result = mutableListOf<R>()
        val items = ConcurrentLinkedQueue(data.toMutableList())
        (1..min(coroutineCount, data.size)).map { coroutine ->
            log.debug { "Starting coroutine $coroutine/$coroutineCount ..." }
            launch {
                var item = items.poll()
                while (item != null) {
                    result += processor(item)
                    item = items.poll()
                }
            }
        }.joinAll()
        result
    }
}

fun String.ensureMaxLength(maxLength: Int): String =
    lines().joinToString("\n") { line ->
        var leftOver = line
        val tmp = StringBuilder()
        do {
            val eat = leftOver.take(maxLength)
            leftOver = leftOver.drop(eat.length)
            tmp.append(eat)
            if (leftOver.isNotEmpty()) tmp.appendLine()
        } while (leftOver.isNotEmpty())
        tmp
    }

fun <T> retry(
    maxAttempts: Int,
    suppressExceptions: List<Class<out Exception>>,
    code: () -> T,
): T =
    doRetry(maxAttempts = maxAttempts, suppressExceptions, currentAttempt = 1, code)

suspend fun <T> retrySuspended(
    maxAttempts: Int,
    suppressExceptions: List<Class<out Exception>>,
    code: suspend () -> T,
): T =
    doRetrySuspend(maxAttempts = maxAttempts, suppressExceptions, currentAttempt = 1, code)

private fun <T> doRetry(
    maxAttempts: Int,
    suppressExceptions: List<Class<out Exception>>,
    currentAttempt: Int,
    code: () -> T,
): T =
    try {
        code()
    } catch (e: Exception) {
        if (suppressExceptions.any { it.kotlin.isSuperclassOf(e::class) } && currentAttempt < maxAttempts) {
            doRetry(maxAttempts = maxAttempts, suppressExceptions, currentAttempt = currentAttempt + 1, code)
        } else throw e
    }

private suspend fun <T> doRetrySuspend(
    maxAttempts: Int,
    suppressExceptions: List<Class<out Exception>>,
    currentAttempt: Int,
    code: suspend () -> T,
): T =
    try {
        code()
    } catch (e: Exception) {
        if (suppressExceptions.any { it.kotlin.isSuperclassOf(e::class) } && currentAttempt < maxAttempts) {
            doRetrySuspend(maxAttempts = maxAttempts, suppressExceptions, currentAttempt = currentAttempt + 1, code)
        } else throw e
    }

fun String.unescape(): String = replace("\\\"", "\"").replace("\\n", "\n")

fun String.firstUpper() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }

