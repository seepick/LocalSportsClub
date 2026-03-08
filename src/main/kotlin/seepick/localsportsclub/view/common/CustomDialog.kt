package seepick.localsportsclub.view.common

import androidx.compose.runtime.Composable

data class CustomDialog(
    val title: String,
    val content: @Composable () -> Unit,
    val confirmLabel: String = "Close",
    val onConfirm: () -> Unit = {},
    val showDismissButton: Boolean = true,
)
