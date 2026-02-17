package seepick.localsportsclub.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.view.activity.ActivitiesScreen
import seepick.localsportsclub.view.common.ConditionalTooltip
import seepick.localsportsclub.view.common.CustomDialog
import seepick.localsportsclub.view.common.CustomSnackbar
import seepick.localsportsclub.view.common.bottomBorder
import seepick.localsportsclub.view.common.brighter
import seepick.localsportsclub.view.freetraining.FreetrainingsScreen
import seepick.localsportsclub.view.notes.NotesScreen
import seepick.localsportsclub.view.preferences.PreferencesScreen
import seepick.localsportsclub.view.preferences.tooltipTextVerifyUscFirst
import seepick.localsportsclub.view.shared.SharedModel
import seepick.localsportsclub.view.usage.UsageView
import seepick.localsportsclub.view.venue.VenueScreen

private val log = logger {}

@Composable
fun MainView(
    viewModel: MainViewModel = koinViewModel(),
    sharedModel: SharedModel = koinInject(),
    snackbarService: SnackbarService = koinInject(),
) {
    var snackbarEvent by remember { mutableStateOf<SnackbarEvent?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(key1 = true) {
        snackbarService.events.collect { event ->
            log.debug { "Going to show snackbar for: $event" }
            snackbarEvent = event
            snackbarHostState.showSnackbar(
                message = if (event.content is SnackbarContent.TextContent) event.content.message else "",
                actionLabel = event.actionLabel,
                duration = event.duration,
            )
        }
    }
    var customDialog: CustomDialog? by sharedModel.customDialog
    if (customDialog != null) {
        AlertDialog(
            onDismissRequest = {
                customDialog = null
            },
            confirmButton = {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = Lsc.colors.primary,
                        contentColor = Color.White,
                    ),
                    onClick = {
                        customDialog!!.onConfirm()
                        customDialog = null
                    }) { Text(customDialog!!.confirmLabel) }
            },
            dismissButton = if (customDialog!!.showDismissButton) {
                {
                    TextButton(
                        colors = if (Lsc.isDarkTheme) {
                            ButtonDefaults.textButtonColors(
                                backgroundColor = Lsc.colors.primary.copy(alpha = 0.3f),
                                contentColor = Color.White,
                            )
                        } else {
                            ButtonDefaults.textButtonColors(
                                backgroundColor = Lsc.colors.primary.brighter(),
                                contentColor = Color.White,
                            )
                        },
                        onClick = {
                            customDialog = null
                        }) { Text("Cancel") }
                }
            } else null,
            title = { Text(customDialog!!.title) },
            text = {
                SelectionContainer {
                    Text(customDialog!!.text)
                }
            },
        )
    }
    Scaffold(snackbarHost = {
        SnackbarHost(
            hostState = snackbarHostState,
        ) { data: SnackbarData ->
            val event = snackbarEvent!!
            CustomSnackbar(
                snackbarData = data,
                event = event,
                content = event.content.let { snackContent ->
                    when (snackContent) {
                        is SnackbarContent.CustomContent -> snackContent.composable
                        is SnackbarContent.TextContent -> {
                            {
                                Text(snackContent.message)
                            }
                        }
                    }
                })
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
            Box(modifier = Modifier.padding(start = 10.dp, end = 10.dp)) {
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
        !viewModel.isSyncPossible, tooltipTextVerifyUscFirst
    ) {
        Button(
            enabled = viewModel.isSyncPossible && !viewModel.isSyncInProgress,
            onClick = { viewModel.startSync() },
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Text(text = "Sync")
        }
    }
    AnimatedVisibility(
        visible = viewModel.isSyncInProgress, enter = fadeIn(), exit = fadeOut()
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center, modifier = Modifier.width(60.dp)
//                .background(Color.Red)
        ) {
            CircularProgressIndicator()
            viewModel.currentSyncStep?.also {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.background(Color.Green),
                ) {
                    Text(it.section, fontSize = 9.sp)
                    // TODO enforce max width!
                    Text(it.detail ?: "", fontSize = 8.sp, color = Color.Gray)
                }
            }
        }
    }
}
