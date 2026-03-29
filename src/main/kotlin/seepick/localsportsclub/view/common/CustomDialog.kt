package seepick.localsportsclub.view.common

import androidx.compose.runtime.Composable

data class CustomDialog(
    val title: String,
    val content: @Composable (Int) -> Unit,
    val confirmLabel: String = "Close",
    val confirmButtonTooltip: String? = null,
    val onConfirm: () -> Unit = {},
    val showDismissButton: Boolean = true,
)
