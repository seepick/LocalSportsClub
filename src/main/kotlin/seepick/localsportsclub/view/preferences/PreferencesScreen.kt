package seepick.localsportsclub.view.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material3.Switch
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
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.model.Country
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.WidthOrFill

private val col1width = 170.dp

@Composable
private fun PreferencesItem(
    label: String, content: @Composable () -> Unit
) {
    Row {
        Text(
            text = label, fontSize = 18.sp, modifier = Modifier.width(col1width).align(Alignment.Top)
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
        PreferencesItem("USC Credentials") {
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
            TextField(
                value = viewModel.entity.uscPassword,
                label = { Text("Password") },
                onValueChange = { viewModel.entity.uscPassword = it },
            )
        }
        PreferencesItem("USC Location") {
            DropDownTextField(
                items = Country.all,
                selectedItem = viewModel.entity.country,
                itemFormatter = { it?.label ?: "" },
                onItemSelected = {
                    viewModel.entity.country = it
                    viewModel.entity.city = null
                },
                label = "Country",
                textSize = WidthOrFill.Width(200.dp),
            )
            Spacer(Modifier.width(10.dp))
            DropDownTextField(
                items = viewModel.entity.country?.cities ?: emptyList(),
                enabled = viewModel.entity.country != null,
                itemFormatter = { it?.label ?: "" },
                selectedItem = viewModel.entity.city,
                onItemSelected = { viewModel.entity.city = it },
                label = "City",
                textSize = WidthOrFill.Width(200.dp),
            )
        }
        PreferencesItem("Google Calendar") {
            Switch(
                checked = viewModel.entity.calendarEnabled, onCheckedChange = {
                    viewModel.entity.calendarEnabled = it
                }, colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedTrackColor = Lsc.colors.primary,
                    uncheckedTrackColor = Lsc.colors.primaryBrighter,
                )
            )
            Spacer(Modifier.width(10.dp))
            TextField(
                value = viewModel.entity.calendarId,
                enabled = viewModel.entity.calendarEnabled,
                label = { Text("Calendar ID") },
                onValueChange = { viewModel.entity.calendarId = it },
            )
        }
        PreferencesItem("Home Coordinates") {
            var latitudeString by remember { mutableStateOf(viewModel.entity.homeLatitude?.toString() ?: "") }
            var longitudeString by remember { mutableStateOf(viewModel.entity.homeLongitude?.toString() ?: "") }
            Spacer(Modifier.width(5.dp))
            TextField(value = latitudeString,
                maxLines = 1,
                isError = latitudeString.isNotEmpty() && latitudeString.toDoubleOrNull() == null,
                label = { Text("Latitude") },
                modifier = Modifier.width(250.dp),
                onValueChange = {
                    latitudeString = it
                    viewModel.onLatitudeEntered(it)
                })
            Spacer(Modifier.width(10.dp))
            TextField(value = longitudeString,
                maxLines = 1,
                isError = longitudeString.isNotEmpty() && longitudeString.toDoubleOrNull() == null,
                label = { Text("Longitude") },
                modifier = Modifier.width(250.dp),
                onValueChange = {
                    longitudeString = it
                    viewModel.onLongitudeEntered(it)
                })
        }

        if (viewModel.entity.isDirty()) {
            Text("Restart the application for changes to take effect.")
        }
    }
}
