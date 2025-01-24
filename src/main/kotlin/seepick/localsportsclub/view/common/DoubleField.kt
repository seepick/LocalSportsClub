package seepick.localsportsclub.view.common

import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DoubleField(
    label: String,
    initialValue: Double?,
    onChange: (Double?) -> Unit,
) {
    var valueString: String by remember { mutableStateOf(initialValue?.toString() ?: "") }
    TextField(label = { Text(label) },
        value = valueString,
        maxLines = 1,
        isError = valueString.isNotEmpty() && valueString.toDoubleOrNull() == null,
        modifier = Modifier.width(200.dp),
        onValueChange = {
            valueString = it
            if (it.isEmpty()) {
                onChange(null)
            } else {
                it.toDoubleOrNull()?.also {
                    onChange(it)
                }
            }
        })
}
