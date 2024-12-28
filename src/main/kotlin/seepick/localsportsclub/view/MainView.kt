package seepick.localsportsclub.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.activity.ActivitiesScreen
import seepick.localsportsclub.view.freetraining.FreetrainingsScreen
import seepick.localsportsclub.view.notes.NotesScreen
import seepick.localsportsclub.view.venue.VenueScreen

@Composable
fun MainView(
    viewModel: MainViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    Column {
        Row {
            val (selectedScreenValue, setSelectedScreen) = viewModel.selectedScreen
            NavigationScreen(selectedScreenValue, setSelectedScreen)
            Spacer(Modifier.width(10.dp))
            Button(enabled = !viewModel.isSyncing, onClick = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        viewModel.startSync()
                    }
                }
            }) {
                Text(text = "Sync")
            }
            AnimatedVisibility(
                visible = viewModel.isSyncing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator()
            }
        }
        when (viewModel.selectedScreen.value) {
            Screen.Activities -> ActivitiesScreen()
            Screen.Freetrainings -> FreetrainingsScreen()
            Screen.Venues -> VenueScreen()
            Screen.Notes -> NotesScreen()
        }
    }
}

