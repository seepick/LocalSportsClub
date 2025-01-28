package seepick.localsportsclub.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.view.activity.ActivitiesScreen
import seepick.localsportsclub.view.common.ConditionalTooltip
import seepick.localsportsclub.view.common.Snackbar2
import seepick.localsportsclub.view.common.bottomBorder
import seepick.localsportsclub.view.freetraining.FreetrainingsScreen
import seepick.localsportsclub.view.notes.NotesScreen
import seepick.localsportsclub.view.preferences.PreferencesScreen
import seepick.localsportsclub.view.usage.UsageView
import seepick.localsportsclub.view.venue.VenueScreen

@Composable
fun MainView(
    viewModel: MainViewModel = koinViewModel(),
    snackbarService: SnackbarService = koinInject(),
) {
    var snackbarType by mutableStateOf(SnackbarType.Info)
    val snackbarHostState = remember {
        SnackbarHostState().also { hostState ->
            val callback: suspend (SnackbarData2) -> SnackbarResult = { data ->
                snackbarType = data.type
                hostState.showSnackbar(
                    message = data.message,
                    actionLabel = data.actionLabel,
                    duration = data.duration,
                )
            }
            snackbarService.initCallback(callback)
        }
    }
    Column {
        Scaffold(snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            ) { data: SnackbarData ->
                Snackbar2(data, snackbarType)
            }
        }) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Lsc.colors.backgroundVariant)
                        .fillMaxWidth(1.0f)
                        .bottomBorder(2.dp, Lsc.colors.primary)
                ) {
                    val (selectedScreenValue, setSelectedScreen) = viewModel.selectedScreen
                    NavigationScreen(selectedScreenValue, setSelectedScreen)
                    Spacer(Modifier.width(10.dp))
                    SyncPanel()
                    Spacer(Modifier.width(10.dp))
                    UsageView()
                }
                when (viewModel.selectedScreen.value) {
                    Screen.Activities -> ActivitiesScreen()
                    Screen.Freetrainings -> FreetrainingsScreen()
                    Screen.Venues -> VenueScreen()
                    Screen.Notes -> NotesScreen()
                    Screen.Preferefences -> PreferencesScreen()
                }
            }
        }
    }
}

@Composable
fun SyncPanel(
    viewModel: MainViewModel = koinViewModel(),
) {
    ConditionalTooltip(
        !viewModel.isSyncPossible, "Please enter your USC credentials in the preferences first."
    ) {
        Button(
            enabled = viewModel.isSyncPossible && !viewModel.isSyncing,
            onClick = { viewModel.startSync() },
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Text(text = "Sync")
        }
    }
    AnimatedVisibility(
        visible = viewModel.isSyncing, enter = fadeIn(), exit = fadeOut()
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(60.dp)
//                .background(Color.Red)
        ) {
            CircularProgressIndicator()
            viewModel.currentSyncStep?.also {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.background(Color.Green),
                ) {
                    Text(it.section, fontSize = 9.sp)
                    Text(it.detail ?: "", fontSize = 8.sp, color = Color.Gray)
                }
            }
        }
    }
}
