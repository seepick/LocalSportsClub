package seepick.localsportsclub.view.common

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import seepick.localsportsclub.Lsc

@Composable
fun BoxScope.LscVScroll(scrollAdapter: androidx.compose.foundation.v2.ScrollbarAdapter) {
    VerticalScrollbar(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight(),
        adapter = scrollAdapter,
        style = LocalScrollbarStyle.current.copy(
            hoverColor = Lsc.colors.scrollbarHover,
            unhoverColor = Lsc.colors.scrollbarUnhover,
        ),
    )
}
