package seepick.localsportsclub.devApp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import seepick.localsportsclub.view.Lsc

object ColorDevApp {
    @JvmStatic
    fun main(args: Array<String>) {
        application {
            Window(
                title = "UI Test App",
                state = WindowState(width = 400.dp, height = 835.dp),
                onCloseRequest = { exitApplication() },
            ) {
                Column {
                    var current = 0.0
                    repeat(101) {
                        Box(
                            modifier = Modifier.size(width = 400.dp, height = 8.dp)
                                .background(Lsc.colors.forTableBg(current))
                        ) {
                            Text(text = String.format("%.2f", current), fontSize = 8.sp)
                        }
                        current += 0.01
                    }
                }
            }
        }
    }
}
