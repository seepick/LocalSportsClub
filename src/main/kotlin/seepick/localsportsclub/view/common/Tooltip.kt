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
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tooltip(
    text: String?,
    content: @Composable () -> Unit,
) {
    if (text == null) {
        content()
    } else {
        TooltipArea(
            tooltip = {
                Surface(
                    modifier = Modifier.shadow(4.dp).let {
                        if (text.length > 60) it.width(400.dp) else it
                    },
                    color = Lsc.colors.backgroundToolip,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = text,
                        color = Lsc.colors.onBackground,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            },
            delayMillis = 600,
            tooltipPlacement = TooltipPlacement.CursorPoint(alignment = Alignment.BottomEnd),
        ) {
            content()
        }
    }
}
