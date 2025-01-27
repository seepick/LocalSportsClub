package seepick.localsportsclub.sync

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.window.singleWindowApplication

fun main() {
    val progress = SyncProgressThreaded()
    val listener = object : SyncProgressListener {
        override fun onSyncStart() {
            println("=> onStart")
        }

        override fun onSyncStep(syncStep: SyncStep) {
            println("=> onStep: $syncStep")
        }

        override fun onSyncFinish() {
            println("=> onFinish")
        }
    }
    progress.register(listener)
    var step = 1
    singleWindowApplication {
        Column {
            Button(onClick = {
                progress.start()
            }) {
                Text("Start")
            }
            Button(onClick = {
                progress.onProgress("Step: ${step++}")
            }) {
                Text("Step")
            }
            Button(onClick = {
                progress.stop()
            }) {
                Text("Stop")
            }
        }
    }
}
