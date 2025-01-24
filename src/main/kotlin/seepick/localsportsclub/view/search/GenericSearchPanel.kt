package seepick.localsportsclub.view.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.view.common.LscHScroll

@Composable
fun GenericSearchPanel(content: @Composable () -> Unit) {
    val state = rememberScrollState()
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.horizontalScroll(state)
                .padding(bottom = 10.dp) // gap for the scrollbar
        ) {
            content()
        }
        LscHScroll(rememberScrollbarAdapter(state))
    }
}
