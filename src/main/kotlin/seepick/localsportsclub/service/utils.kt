package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentLinkedQueue

private val log = logger {}

fun <T> workParallel(coroutineCount: Int, data: List<T>, processor: suspend (T) -> Unit) {
    val items = ConcurrentLinkedQueue(data.toMutableList())
    runBlocking {
        (1..coroutineCount).map { coroutine ->
            log.debug { "Starting coroutine $coroutine/$coroutineCount ..." }
            launch {
                var item = items.poll()
                while (item != null) {
                    processor(item)
                    item = items.poll()
                }
            }
        }.joinAll()
    }
}
