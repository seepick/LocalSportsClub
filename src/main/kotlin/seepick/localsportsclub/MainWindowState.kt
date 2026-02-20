package seepick.localsportsclub

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.oshai.kotlinlogging.KotlinLogging.logger

class MainWindowState {
    private val log = logger {}

    var width by mutableStateOf(0)
        private set
    var height by mutableStateOf(0)
        private set

    fun update(width: Int, height: Int) {
        log.trace { "update($width x $height)" }
        this.width = width
        this.height = height
    }
}
