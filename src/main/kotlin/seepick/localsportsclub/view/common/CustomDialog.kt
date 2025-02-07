package seepick.localsportsclub.view.common

data class CustomDialog(
    val title: String,
    val text: String,
    val confirmLabel: String,
    val onConfirm: () -> Unit,
)
