package seepick.localsportsclub.view.search

import androidx.compose.runtime.Composable
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.search.SearchOption
import seepick.localsportsclub.view.common.ClickableText
import seepick.localsportsclub.view.common.darker

@Composable
fun SearchOption<*>.buildClickableText() {
    ClickableText(text = label,
        notHoveredColor = if (enabled) Lsc.colors.primary else Lsc.colors.primary.darker(),
        onClicked = { updateEnabled(!enabled) })
}
