package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.min

private val log = logger {}

suspend fun <T, R> workParallel(coroutineCount: Int, data: List<T>, processor: suspend (T) -> R): List<R> {
    return coroutineScope {
        val result = mutableListOf<R>()
        withContext(Dispatchers.IO) {
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
        }
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
