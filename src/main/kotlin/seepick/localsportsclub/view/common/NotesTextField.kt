package seepick.localsportsclub.view.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
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
    val state = rememberScrollState()
    Box {
        OutlinedTextField(
            label = { Text("Notes") },
            value = notes,
            enabled = enabled,
            onValueChange = setter,
            modifier = modifier.fillMaxWidth()
                .verticalScroll(state),
        )
        LscVScroll(rememberScrollbarAdapter(state))
    }
}
