package seepick.localsportsclub.view.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun LabeledText(label: String, text: String) {
    Row {
        Text("$label: ", fontWeight = FontWeight.Bold)
        Text(text)
    }
}
