package seepick.localsportsclub.view.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun LongText(label: String? = null, text: String, onShowLongText: ((String) -> Unit)? = null) {
    var isOverflowing by remember { mutableStateOf(false) }
    Row {
        if (label != null) {
            Text("$label: ", fontWeight = FontWeight.Bold)
        }
        Tooltip(text) {
            Text(
                text = text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = {
                    isOverflowing = it.hasVisualOverflow
                    println("isOverflowing=$isOverflowing")
                },
                modifier = Modifier.clickable(
                    enabled = isOverflowing && onShowLongText != null
                ) {
                    onShowLongText!!(text)
                }
            )
        }
    }
}
