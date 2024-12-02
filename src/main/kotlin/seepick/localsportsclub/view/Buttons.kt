package seepick.localsportsclub.view

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import seepick.localsportsclub.logic.PartnersService

@Composable
fun Buttons(
    partnersService: PartnersService = koinInject(),
) {
//    val myService = koinInject<Service>()
    MaterialTheme {
        Row {
            Button(onClick = {
                partnersService.insert()
            }) {
                Text("Save")
            }
            Button(onClick = {
                partnersService.select()
            }) {
                Text("Load")
            }
        }
    }
}