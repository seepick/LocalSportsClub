package seepick.localsportsclub.view.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment

@Composable
fun CheckboxText(label: String, enabled: Boolean, isTrue: MutableState<Boolean>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isTrue.value,
            onCheckedChange = { isTrue.value = it },
            enabled = enabled,
        )
        Text(label)
    }
}
