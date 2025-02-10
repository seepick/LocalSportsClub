package seepick.localsportsclub.view.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
fun SmallButton(
    label: String,
    size: DpSize,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        contentPadding = PaddingValues(0.dp),
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
            .size(size),
    ) {
        Text(label)
    }
}
