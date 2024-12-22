package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger

private val log = logger {}

suspend fun <T> workParallel(coroutineCount: Int, data: List<T>, processor: suspend (T) -> Unit) {
    data.forEach {
        processor.invoke(it)
    }
//    coroutineScope {
//        val items = ConcurrentLinkedQueue(data.toMutableList())
//        (1..coroutineCount).map { coroutine ->
//            log.debug { "Starting coroutine $coroutine/$coroutineCount ..." }
//            launch {
//                var item = items.poll()
//                while (item != null) {
//                    processor(item)
//                    item = items.poll()
//                }
//            }
//        }.joinAll()
//    }
}
