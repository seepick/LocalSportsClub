package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

private val log = logger {}

suspend fun <T> workParallel(coroutineCount: Int, data: List<T>, processor: suspend (T) -> Unit) {
    coroutineScope {
        val items = ConcurrentLinkedQueue(data.toMutableList())
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
