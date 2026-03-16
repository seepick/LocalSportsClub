package seepick.localsportsclub.view.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SuperSmallButton(
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

@Composable
fun SmallButton(
    text: String,
    icon: ImageVector? = null,
    tooltip: String? = null,
    onClick: () -> Unit,
) {
    Tooltip(tooltip) {
        Button(
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            modifier = Modifier.height(25.dp),
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null)
            }
            Text(text, fontSize = 10.sp)
        }
    }
}
