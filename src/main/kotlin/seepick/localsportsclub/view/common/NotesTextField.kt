package seepick.localsportsclub.view.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NotesTextField(
    notes: String,
    setter: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        label = { Text("Notes") },
//        maxLines = 4,
        value = notes,
        enabled = enabled,
        onValueChange = setter,
        modifier = modifier.fillMaxWidth()
    )
}
