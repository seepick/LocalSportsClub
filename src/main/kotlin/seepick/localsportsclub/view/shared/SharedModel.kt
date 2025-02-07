package seepick.localsportsclub.view.shared

import androidx.compose.runtime.mutableStateOf
import seepick.localsportsclub.view.common.CustomDialog

class SharedModel {
    val shouldGcalBeManaged = mutableStateOf(true)
    val isUscConnectionVerified = mutableStateOf(false)
    val verifiedGcalId = mutableStateOf<String?>(null)
    val verifiedUscUsername = mutableStateOf<String?>(null)
    val verifiedUscPassword = mutableStateOf<String?>(null)

    val customDialog = mutableStateOf<CustomDialog?>(null)
}
