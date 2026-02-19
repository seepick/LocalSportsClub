@file:OptIn(DelicateCoroutinesApi::class)

package playground

import ch.qos.logback.classic.Level
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import seepick.localsportsclub.reconfigureLog
import kotlin.time.measureTime

object CoroutinesPlayground {
    // -Dkotlinx.coroutines.debug JVM option
    // https://www.youtube.com/watch?v=VWlwkqmTLHc
    // https://www.youtube.com/watch?v=ZX8VsqNO_Ss
    // https://www.youtube.com/watch?v=4_fDefmI3yI
    // https://www.youtube.com/watch?v=ZX8VsqNO_Ss flows
    private val log: KLogger

    init {
        reconfigureLog(null, mapOf("playground" to Level.DEBUG))
        log = logger {}
    }

    @JvmStatic
    fun main(args: Array<String>) {

        log.debug { "main BEFORE" } // [main]
        runBlocking {
//            debugThreadNames()
//            launchRunsAsync()
//            cancellation()
            asyncAwait()
        }
        log.debug { "main AFTER" }
    }

    private suspend fun launchRunsAsync() = coroutineScope {
        launch(Dispatchers.IO) {
            delay(3_000L)
            log.debug { "IO #1 done" }
        }
        launch(Dispatchers.IO) {
            delay(3_000L)
            log.debug { "IO #2 done" }
        }
        delay(2_000L)
        log.debug { "Parent coroutine done." }
    }

    private fun CoroutineScope.debugThreadNames() {
        launch(Dispatchers.IO) {
            log.debug { "Coroutine on IO START" } // [DefaultDispatcher-worker-1]
            delay(500) // suspends the coroutine, not (blocking) the whole thread
            withContext(Dispatchers.Main) {
                log.debug { "switched context to Main" } // [AWT-EventQueue-0]
            }
            log.debug { "Coroutine END" }
        }
    }

    private fun cancellation() {
        val job = GlobalScope.launch {
            withTimeout(5_000L) {
                while (isActive) {
                    log.debug { "Doing some work..." }
                    delay(500L) // delay itself supports cancellation already -yay :)
                    ensureActive() // if (!isActive) { throw CancellationException() }
                }
                log.debug { "Finished work" } // will not be invoked for timeout
            }
        }
        runBlocking {
            log.debug { "Waiting for job." }
            job.join()
//            delay(2_000L)
//            log.debug { "Cancelling job" }
//            job.cancel()
        }
    }

    private suspend fun asyncAwait() {
        log.debug { "async START" }
        val job = GlobalScope.launch(Dispatchers.IO) {
            val timeNeeded = measureTime {
                // will take 2x 2secs
//                val response1 = heavyLoad()
//                val response2 = heavyLoad()
//                log.debug { "Answers: $response1, $response2" }

                // will take only 1x 2secs (parallelization FTW)
                val response1 = async { heavyLoad() }
                val response2 = async { heavyLoad() }
                val r1 = response1.await()
                val r2 = response2.await()
                log.debug { "Answers: $r1, $r2" }
            }
            log.debug { "Time needed: ${timeNeeded.inWholeMilliseconds}" }
        }
        job.join() // in real life application, we wouldn't do this, but keep the app alive through UI loop
        log.debug { "async END" }
    }

    suspend fun manyApiCalls(ids: List<Int> = listOf(1, 2, 3)): List<String> = coroutineScope {
        // async ... start a coroutine for every element at the same time
        ids.map { async { apiCall(it) } }
    }.awaitAll() // handy function to .map { it.await() }

    suspend fun heavyLoad(): Any {
        delay(2000L)
        return 42
    }

    suspend fun apiCall(id: Int): String {
        delay(1000L)
        return "id$id"
    }
}
