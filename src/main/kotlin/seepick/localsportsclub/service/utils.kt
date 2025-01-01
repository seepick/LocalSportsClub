package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.coobird.thumbnailator.Thumbnails
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentLinkedQueue

private val log = logger {}

suspend fun <T, R> workParallel(coroutineCount: Int, data: List<T>, processor: suspend (T) -> R): List<R> {
//    return data.map {
//        processor.invoke(it)
//    }
    return coroutineScope {
        val result = mutableListOf<R>()
        withContext(Dispatchers.IO) {
//        withContext(Dispatchers.IO) {
            val items = ConcurrentLinkedQueue(data.toMutableList())
            (1..coroutineCount).map { coroutine ->
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

fun resizeImage(original: ByteArray, size: Pair<Int, Int>, format: String = "png"): ByteArray {
    val output = ByteArrayOutputStream()
    Thumbnails.of(original.inputStream())
        .keepAspectRatio(true)
        .size(size.first, size.second)
        .outputFormat(format)
        .outputQuality(1.0)
        .toOutputStream(output)
    return output.toByteArray()
}
