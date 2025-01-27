package playground

import androidx.compose.ui.window.singleWindowApplication
import ch.qos.logback.classic.Level
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import seepick.localsportsclub.reconfigureLog

object CoroutinesPlayground {
    // https://www.youtube.com/watch?v=2QInrEaXyMo
    // https://www.youtube.com/watch?v=VWlwkqmTLHc

    @JvmStatic
    fun main(args: Array<String>) {
        reconfigureLog(false, mapOf("playground" to Level.DEBUG))
        val log = logger {}
        log.debug { "main BEFORE" } // [main]
        singleWindowApplication {
            log.debug { "compose START" } // [AWT-EventQueue-0]
            GlobalScope.launch {
                log.debug { "Coroutine START" } // [DefaultDispatcher-worker-1]
                delay(1_000) // suspends the coroutine, not (blocking) the whole thread
                log.debug { "Coroutine END" }
            }
            log.debug { "compose END" } // [AWT-EventQueue-0]
        }
        log.debug { "main AFTER" }
    }
}
