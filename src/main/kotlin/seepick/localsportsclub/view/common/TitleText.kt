package seepick.localsportsclub.view.common

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun TitleText(
    text: String,
    textDecoration: TextDecoration? = null,
    modifier: Modifier = Modifier,
) {
    var isOverflowing by remember { mutableStateOf(false) }
    Tooltip(if (isOverflowing) text else null) {
        SelectionContainer {
            Text(
                text = text,
                onTextLayout = { isOverflowing = it.hasVisualOverflow },
                style = MaterialTheme.typography.h1,
                textDecoration = textDecoration,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = modifier,
            )
        }
    }
}
