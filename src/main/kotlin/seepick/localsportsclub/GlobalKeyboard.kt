package seepick.localsportsclub

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.view.Screen

class GlobalKeyboard {

    private val log = logger {}
    private val listeners = mutableListOf<GlobalKeyboardListener>()

    var isMetaKeyDown = false
        private set

    fun registerListener(listener: GlobalKeyboardListener) {
        listeners += listener
    }

    fun process(event: KeyEvent) {
        if (event.key == Key.MetaLeft || event.key == Key.MetaRight) {
            isMetaKeyDown = event.type == KeyEventType.KeyDown
            log.debug { "changed meta key down to: $isMetaKeyDown" }
        }
        if (event.type == KeyEventType.KeyDown && event.isMetaPressed) {
            when (event.key) {
                Key.One -> changeToScreen(1)
                Key.Two -> changeToScreen(2)
                Key.Three -> changeToScreen(3)
                Key.Four -> changeToScreen(4)
                Key.Five -> changeToScreen(5)
            }
        }
    }

    private fun changeToScreen(screenNr: Int) {
        val screen = Screen.entries.first { it.ordinal == (screenNr - 1) }
        listeners.forEach {
            it.onKeyboardChangeScreen(screen)
        }
    }
}

interface GlobalKeyboardListener {
    fun onKeyboardChangeScreen(screen: Screen)
}
