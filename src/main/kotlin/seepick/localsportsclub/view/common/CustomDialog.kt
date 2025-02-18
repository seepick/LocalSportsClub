package seepick.localsportsclub.view.common

data class CustomDialog(
    val title: String,
    val text: String,
    val confirmLabel: String = "Close",
    val onConfirm: () -> Unit = {},
    val showDismissButton: Boolean = true,
)
