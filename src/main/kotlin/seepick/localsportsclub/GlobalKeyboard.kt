package seepick.localsportsclub

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

class GlobalKeyboard {

    private val listeners = mutableListOf<GlobalKeyboardListener>()

    fun registerListener(listener: GlobalKeyboardListener) {
        listeners += listener
    }

    fun process(event: KeyEvent) {
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
        listeners.forEach {
            it.onKeyboardChangeScreen(screenNr)
        }
    }
}

interface GlobalKeyboardListener {
    fun onKeyboardChangeScreen(screenNr: Int)
}
