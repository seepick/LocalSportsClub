package seepick.localsportsclub.view.preferences

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.model.Country
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.PasswordField
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.WidthOrFill

private val col1width = 170.dp

@Composable
private fun PreferencesItem(
    label: String, content: @Composable () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 18.sp,
            modifier = Modifier.width(col1width).align(Alignment.Top),
        )
        content()
    }
}

@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel = koinViewModel()
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(30.dp)) {
        PreferencesItem("Credentials") {
            TextField(
                value = viewModel.entity.uscUsername,
                label = { Text("Username") },
                onValueChange = { viewModel.entity.uscUsername = it },
                modifier = Modifier.onPreviewKeyEvent {
                    if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                        focusManager.moveFocus(FocusDirection.Right)
                        true
                    } else {
                        false
                    }
                },
            )
            Spacer(Modifier.width(10.dp))
            PasswordField(
                password = viewModel.entity.uscPassword,
                onChange = { viewModel.entity.uscPassword = it }
            )
            Spacer(Modifier.width(10.dp))
            Button(
                enabled = viewModel.entity.uscUsername.isNotEmpty() && !viewModel.isUscConnectingTesting && viewModel.entity.uscPassword.isNotEmpty(),
                onClick = {
                    viewModel.testUscConnection()
                },
            ) {
                Text("Test Connection")
            }
            AnimatedVisibility(
                visible = viewModel.isUscConnectingTesting, enter = fadeIn(), exit = fadeOut()
            ) {
                CircularProgressIndicator()
            }
        }
        PreferencesItem("USC Info") {
            Tooltip("First day of the month when the USC period start", offset = true) {
                TextField(value = viewModel.entity.periodFirstDay?.toString() ?: "",
                    label = { Text("Period") },
                    modifier = Modifier.width(120.dp),
                    onValueChange = {
                        if (it.isEmpty()) {
                            viewModel.entity.periodFirstDay = null
                        } else {
                            it.toIntOrNull()?.also {
                                viewModel.entity.periodFirstDay = it
                            }
                        }
                    })
            }
            Spacer(Modifier.width(10.dp))
            DropDownTextField(
                label = "Country",
                items = Country.all,
                selectedItem = viewModel.entity.country,
                itemFormatter = { it?.label ?: "" },
                onItemSelected = {
                    viewModel.entity.country = it
                    viewModel.entity.city = null
                },
                textSize = WidthOrFill.Width(180.dp),
            )
            Spacer(Modifier.width(10.dp))
            DropDownTextField(
                label = "City",
                items = viewModel.entity.country?.cities ?: emptyList(),
                enabled = viewModel.entity.country != null,
                itemFormatter = { it?.label ?: "" },
                selectedItem = viewModel.entity.city,
                onItemSelected = { viewModel.entity.city = it },
                textSize = WidthOrFill.Width(250.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text("Plan: ${viewModel.plan?.label ?: "N/A"}")
        }
        PreferencesItem("Home Coordinates") {
            var latitudeString by remember { mutableStateOf(viewModel.entity.homeLatitude?.toString() ?: "") }
            var longitudeString by remember { mutableStateOf(viewModel.entity.homeLongitude?.toString() ?: "") }
            Spacer(Modifier.width(5.dp))
            TextField(label = { Text("Latitude") },
                value = latitudeString,
                maxLines = 1,
                isError = latitudeString.isNotEmpty() && latitudeString.toDoubleOrNull() == null,
                modifier = Modifier.width(200.dp),
                onValueChange = {
                    latitudeString = it
                    viewModel.onLatitudeEntered(it)
                })
            Spacer(Modifier.width(10.dp))
            TextField(label = { Text("Longitude") },
                value = longitudeString,
                maxLines = 1,
                isError = longitudeString.isNotEmpty() && longitudeString.toDoubleOrNull() == null,
                modifier = Modifier.width(200.dp),
                onValueChange = {
                    longitudeString = it
                    viewModel.onLongitudeEntered(it)
                })
        }
        PreferencesItem("Google Calendar") {
            Switch(checked = viewModel.entity.calendarEnabled, onCheckedChange = {
                viewModel.entity.calendarEnabled = it
            })
            Spacer(Modifier.width(10.dp))
            TextField(
                label = { Text("Calendar ID") },
                value = viewModel.entity.calendarId,
                enabled = viewModel.entity.calendarEnabled,
                onValueChange = { viewModel.entity.calendarId = it },
                modifier = Modifier.width(500.dp)
            )
            Spacer(Modifier.width(10.dp))
            Button(
                enabled = viewModel.entity.calendarEnabled && !viewModel.isGcalConnectingTesting && viewModel.entity.calendarId.isNotEmpty(),
                onClick = {
                    viewModel.testGcalConnection()
                },
            ) {
                Text("Test Connection")
            }
            AnimatedVisibility(
                visible = viewModel.isGcalConnectingTesting, enter = fadeIn(), exit = fadeOut()
            ) {
                CircularProgressIndicator()
            }
        }

        if (viewModel.entity.isDirty()) {
            Text("Restart the application for changes to take effect.")
        }
    }
}
