package seepick.localsportsclub.view

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import seepick.localsportsclub.logic.VenuesService
import seepick.localsportsclub.sync.Syncer

@Composable
fun MainWindow(
    venuesService: VenuesService = koinInject(),
    syncer: Syncer = koinInject(),
) {
//    val myService = koinInject<Service>()
    MaterialTheme {
        Row {
            Button(onClick = {
                venuesService.insert()
            }) {
                Text("Save")
            }
            Button(onClick = {
                venuesService.select()
            }) {
                Text("Load")
            }
            Button(onClick = {
                syncer.sync()
            }) {
                Text("Sync")
            }
        }
    }
}
