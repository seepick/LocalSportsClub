package seepick.localsportsclub.view.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun LabeledText(label: String, text: String) {
    Row {
        Text("$label: ", fontWeight = FontWeight.Bold)
        Tooltip(text) {
            Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
