package seepick.localsportsclub.view.common

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@Composable
fun TitleText(
    text: String,
    textDecoration: TextDecoration? = null,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        fontSize = 25.sp,
        textDecoration = textDecoration,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colors.primary,
        modifier = modifier,
    )
}
