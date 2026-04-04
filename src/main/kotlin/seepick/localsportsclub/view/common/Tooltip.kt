package seepick.localsportsclub.view.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.venue.detail.XString


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tooltip(
    text: String?,
    content: @Composable () -> Unit,
) {
    Tooltip(XString(text), content)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tooltip(
    text: XString?,
    content: @Composable () -> Unit,
) {
    if (text == null) {
        content()
    } else {
        TooltipArea(
            tooltip = {
                Surface(
                    modifier = Modifier.shadow(4.dp).let {
                        if (text.asString.length > 60) it.width(400.dp) else it
                    }
//                     TODO verify this works   .widthIn(max = 400.dp)
                    ,
                    color = Lsc.colors.backgroundToolip,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = text.asAnnotatedString,
                        color = Lsc.colors.onBackground,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            },
            tooltipPlacement = TooltipPlacement.CursorPoint(
                alignment = Alignment.BottomEnd,
                offset = DpOffset(5.dp, 5.dp),
            ),
        ) {
            content()
        }
    }
}
