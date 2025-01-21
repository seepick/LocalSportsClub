package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.search.SearchOption
import seepick.localsportsclub.view.common.ClickableText
import seepick.localsportsclub.view.common.darker

@Composable
fun SearchOption<*>.ClickableSearchText() {
    ClickableText(text = label,
        notHoveredColor = if (enabled) Lsc.colors.primary else Lsc.colors.primary.darker(),
        onClick = { updateEnabled(!enabled) })
    Spacer(modifier = Modifier.width(5.dp))
}
