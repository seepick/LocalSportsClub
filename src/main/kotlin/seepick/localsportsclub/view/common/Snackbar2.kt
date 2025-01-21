package seepick.localsportsclub.view.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc

@Composable
fun Snackbar2(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val actionLabel = snackbarData.actionLabel
    val actionComposable: (@Composable () -> Unit)? = if (actionLabel != null) {
        @Composable {
            Button(
                onClick = { snackbarData.performAction() },
            ) {
                Text(actionLabel)
            }
        }
    } else {
        null
    }
    Snackbar(
        modifier = modifier.padding(12.dp).border(1.dp, Lsc.colors.primary),
        content = { Text(snackbarData.message) },
        action = actionComposable,
        actionOnNewLine = false,
        shape = MaterialTheme.shapes.small,
        backgroundColor = Lsc.colors.surface,
        contentColor = Lsc.colors.surface, // what for?!
        elevation = 6.dp,
    )
}
