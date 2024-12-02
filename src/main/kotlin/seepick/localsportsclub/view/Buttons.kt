package seepick.localsportsclub.view

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import seepick.localsportsclub.logic.Service

@Composable
fun Buttons(
    myService: Service = koinInject()
) {
//    val myService = koinInject<Service>()

    MaterialTheme {
        Row {
            Button(onClick = {
                println("clicked: ${myService.say()}")
            }) {
                Text("Haha")
            }
        }
    }
}