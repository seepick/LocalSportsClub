package seepick.localsportsclub.view.common

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.view.Lsc

val scrollbarWidthPadding = 12.dp

@Composable
fun BoxScope.LscVScroll(
    scrollAdapter: androidx.compose.foundation.v2.ScrollbarAdapter,
    gapTop: Dp = 0.dp,
    gapBottom: Dp = 0.dp,
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .width(scrollbarWidthPadding)
            .fillMaxHeight()
//            .background(Color.Blue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
//                .background(Color.Cyan)
        ) {
            if (gapTop != 0.dp) {
                Spacer(
                    modifier = Modifier
                        .width(scrollbarWidthPadding)
                        .height(gapTop)
//                        .background(Color.Red)
                )
            }
            Box(
                modifier = Modifier
                    .width(scrollbarWidthPadding)
                    .weight(1f)
            ) {
                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .fillMaxHeight(),
                    adapter = scrollAdapter,
                    style = LocalScrollbarStyle.current.copy(
                        hoverColor = Lsc.colors.scrollbarHover,
                        unhoverColor = Lsc.colors.scrollbarUnhover,
                    ),
                )
            }
            if (gapBottom != 0.dp) {
                Spacer(
                    modifier = Modifier
                        .width(scrollbarWidthPadding)
                        .height(gapBottom)
                        .background(Color.Green)
                )
            }
        }
    }
}

@Composable
fun BoxScope.LscHScroll(scrollAdapter: androidx.compose.foundation.v2.ScrollbarAdapter) {
    HorizontalScrollbar(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth(),
        adapter = scrollAdapter,
        style = LocalScrollbarStyle.current.copy(
            hoverColor = Lsc.colors.scrollbarHover,
            unhoverColor = Lsc.colors.scrollbarUnhover,
        ),
    )
}
