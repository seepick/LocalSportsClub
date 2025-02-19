package seepick.localsportsclub.view.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.view.SnackbarEvent
import seepick.localsportsclub.view.SnackbarType

private val SnackbarType.color
    get() = when (this) {
        SnackbarType.Info -> Lsc.colors.surface
        SnackbarType.Warn -> Color.Yellow.copy(alpha = 0.3f).compositeOver(Lsc.colors.surface)
        SnackbarType.Error -> Color.Red.copy(alpha = 0.3f).compositeOver(Lsc.colors.surface)
    }

@Composable
fun CustomSnackbar(
    snackbarData: SnackbarData,
    event: SnackbarEvent,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val actionLabel = snackbarData.actionLabel
    val actionComposable: (@Composable () -> Unit)? = if (actionLabel != null) {
        @Composable {
            Button(
                onClick = {
                    snackbarData.performAction() // to close the snackbar dialog
                    event.onResult?.invoke(SnackbarResult.ActionPerformed) // to execute the actual logic
                },
            ) {
                Text(actionLabel)
            }
        }
    } else {
        null
    }
    Snackbar(
        modifier = modifier.padding(12.dp).border(1.dp, Lsc.colors.primary),
        content = content,
        action = actionComposable,
        actionOnNewLine = false,
        shape = MaterialTheme.shapes.small,
        backgroundColor = event.type.color,
        contentColor = Lsc.colors.surface, // what for?!
        elevation = 6.dp,
    )
}
