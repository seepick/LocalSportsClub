package seepick.localsportsclub.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.venue.VenuePanel

@Composable
fun MainWindow(
//    syncer: Syncer = koinInject(),
    viewModel: MainViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    Column {
        Row {
            Button(enabled = !viewModel.isSyncing, onClick = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        viewModel.startSync()
                    }
                }
            }) {
                Text(text = "Sync")
            }
            if (viewModel.isSyncing) {
                CircularProgressIndicator()
            }
        }
        VenuePanel()
    }
}

