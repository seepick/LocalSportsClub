package seepick.localsportsclub.view.preferences

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.seepick.uscclient.model.Country
import com.github.seepick.uscclient.plan.Plan
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.model.GlobalRemarkType
import seepick.localsportsclub.view.LocalTextFieldColors
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.DoubleField
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.PasswordField
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrFill
import seepick.localsportsclub.view.common.composeIt
import seepick.localsportsclub.view.remark.GlobalRemarkViewModel

private val col1width = 180.dp

const val tooltipTextVerifyUscFirst = "Enter and verify your USC login data in the preferences first"

@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel = koinViewModel(),
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(30.dp),
        modifier = Modifier.padding(25.dp),
    ) {
        PreferencesItem("Credentials") {
            CredentialsRow()
        }
        PreferencesItem("Location") {
            UscLocationRow()
        }
        PreferencesItem("Coordinates") {
            DoubleField(
                label = "Latitude",
                initialValue = viewModel.entity.homeLatitude,
                onChange = { viewModel.onLatitudeChanged(it) },
            )
            Spacer(Modifier.width(5.dp))
            DoubleField(
                label = "Longitude",
                initialValue = viewModel.entity.homeLongitude,
                onChange = { viewModel.onLongitudeChanged(it) },
            )
        }
        PreferencesItem("Membership") {
            UscPlanRow()
        }
        PreferencesItem("Google Calendar") {
            GCalRow()
        }
        PreferencesItem("Remarks") {
            RemarksRow()
        }

        if (viewModel.entity.isDirty()) {
            Text("Restart the application for changes to take effect.")
        }
    }
}

@Composable
private fun PreferencesItem(
    label: String, content: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(col1width).align(Alignment.CenterVertically),
        )
        content()
    }
}

@Composable
private fun CredentialsRow(
    viewModel: PreferencesViewModel = koinViewModel(),
) {
    val focusManager = LocalFocusManager.current

    TextField(
        value = viewModel.entity.uscUsername,
        label = { Text("Username") },
        onValueChange = { viewModel.setUscUsername(it) },
        colors = LocalTextFieldColors.current,
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
    PasswordField(password = viewModel.entity.uscPassword, onChange = { viewModel.setUscPassword(it) })
    Spacer(Modifier.width(10.dp))
    Button(
        enabled = viewModel.entity.uscUsername.isNotEmpty() && !viewModel.isUscConnectionVerifying && viewModel.entity.uscPassword.isNotEmpty() && viewModel.verifiedUscUsername.value != viewModel.entity.uscUsername && viewModel.verifiedUscPassword.value != viewModel.entity.uscPassword,
        onClick = { viewModel.verifyUscConnection() },
    ) {
        Text("Verify Login")
    }
    AnimatedVisibility(
        visible = viewModel.isUscConnectionVerifying, enter = fadeIn(), exit = fadeOut()
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun UscPlanRow(
    viewModel: PreferencesViewModel = koinViewModel(),
) {
    PeriodTextField(viewModel.entity.periodFirstDay)
    Spacer(Modifier.width(10.dp))

    Text(buildString {
        append("Plan: ")
        viewModel.plan?.also { plan ->
            if (plan is Plan.OnefitPlan) {
                append("${plan.uscPlan.fullLabel} (${plan.label})")
            } else {
                append(plan.fullLabel)
            }
        } ?: append("N/A")
    })
}

@Composable
fun UscLocationRow(
    viewModel: PreferencesViewModel = koinViewModel(),
) {
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
}

@Composable
private fun PeriodTextField(periodFirstDay: MutableState<Int?>) {
    var periodFirstDayString by remember { mutableStateOf(periodFirstDay.value?.toString() ?: "") }

    Tooltip("The day of the month when the check-in period starts (between 1 and 28)") {
        TextField(
            value = periodFirstDayString,
            label = { Text("Period Day") },
            colors = LocalTextFieldColors.current,
            modifier = Modifier.width(120.dp),
            isError = if (periodFirstDayString.isEmpty()) {
                false
            } else if (periodFirstDayString.toIntOrNull() == null) {
                true
            } else {
                periodFirstDayString.toInt() !in 1..28
            },
            onValueChange = {
                periodFirstDayString = it
                if (it.isEmpty()) {
                    periodFirstDay.value = null
                } else {
                    it.toIntOrNull()?.also {
                        if (it in 1..28) {
                            periodFirstDay.value = it
                        }
                    }
                }
            })
    }
}


@Composable
fun RemarksButton(
    label: String,
    icon: VisualIndicator,
    tooltip: String,
    onClick: () -> Unit,
) {
    Tooltip(tooltip) {
        Button(onClick = onClick) {
            icon.composeIt(paddingEnd = 5.dp)
            Text(label)
        }
    }
}

@Composable
fun RemarksRow(
    viewModel: GlobalRemarkViewModel = koinViewModel(),
) {
    RemarksButton(
        label = "Category Remarks",
        icon = Lsc.icons.categoryIndicator,
        tooltip = "Open category remarks dialog ...",
        onClick = { viewModel.onViewDialog(GlobalRemarkType.Category) },
    )
    Spacer(Modifier.width(5.dp))
    RemarksButton(
        label = "Activity Remarks",
        icon = Lsc.icons.activitiesIndicator,
        tooltip = "Open activity remarks dialog ...",
        onClick = { viewModel.onViewDialog(GlobalRemarkType.Activity) },
    )
    Spacer(Modifier.width(5.dp))
    RemarksButton(
        label = "Teacher Remarks",
        icon = Lsc.icons.teachersIndicator,
        tooltip = "Open teacher remarks dialog ...",
        onClick = { viewModel.onViewDialog(GlobalRemarkType.Teacher) },
    )
}

@Composable
fun GCalRow(
    viewModel: PreferencesViewModel = koinViewModel(),
) {
    Switch(
        checked = viewModel.entity.calendarEnabled,
        onCheckedChange = { viewModel.entity.calendarEnabled = it },
        colors = SwitchDefaults.colors(uncheckedThumbColor = Lsc.colors.clickableNeutral),
    )
    Spacer(Modifier.width(10.dp))
    TextField(
        label = { Text("Calendar ID") },
        value = viewModel.entity.calendarId,
        enabled = viewModel.entity.calendarEnabled,
        colors = LocalTextFieldColors.current,
        onValueChange = { viewModel.setCalendarId(it) },
        modifier = Modifier.width(500.dp)
    )
    Spacer(Modifier.width(10.dp))
    Button(
        enabled = viewModel.entity.calendarEnabled && !viewModel.isGcalConnectionVerifying && viewModel.entity.calendarId.isNotEmpty() && viewModel.verifiedGcalId.value != viewModel.entity.calendarId,
        onClick = {
            viewModel.verifyGcalConnection()
        },
    ) {
        Text("Verify Login")
    }
    AnimatedVisibility(
        visible = viewModel.isGcalConnectionVerifying, enter = fadeIn(), exit = fadeOut()
    ) {
        CircularProgressIndicator()
    }
    Spacer(Modifier.width(5.dp))

    // GCal trash creds file if: com.google.api.client.auth.oauth2.TokenResponseException: 400 Bad Request
    // { "error": "invalid_grant", "error_description": "Token has been expired or revoked." }
    Tooltip("Do this when you get an error message saying that your token has been expired/revoked.") {
        Button(
            enabled = viewModel.entity.calendarEnabled,
            onClick = { viewModel.resetTokenCache() },
        ) {
            Text("Reset Cache")
        }
    }
}
